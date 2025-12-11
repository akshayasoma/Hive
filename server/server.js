import express from "express";
import mongoose from "mongoose";
import cors from "cors";
import dotenv from "dotenv";

dotenv.config();

const app = express();
app.use(express.json());
app.use(cors());

// connect to MongoDB Atlas
console.log(process.env.MONGO_URI)
mongoose.connect(process.env.MONGO_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
})
.then(() => console.log("Connected to MongoDB"))
.catch(err => console.error("MongoDB connection error:", err));


// schema
const groupSchema = new mongoose.Schema({
    groupName: String,
    creatorName: String,
    groupId: String,
    creatorId: String,
    peopleList: [String],

    groceries: [
      {
        name: { type: String, required: true },
        description: { type: String, default: "" },
        quantity: { type: Number, default: 1 },
        completed: { type: Boolean, default: false }
      }
    ],

    chores: [
      {
        name: { type: String, required: true },
        description: { type: String, default: "" },
        points: { type: Number, default: 0 },
        status: { type: Number, default: 0 },
        assignee: { type: String, default: "" }
      }
    ]
});

const Group = mongoose.model("Group", groupSchema);

// User schema
const userSchema = new mongoose.Schema({
  userId: String,        // deviceId
  name: String,          // userName
  preferences: {
                   type: Object,
                   default: {}
                 },
  points: {
           type: Number,
           default: 0
         },
  profilePic: {
               type: String,
               default: ""
             },
 choreRegister: {
     type: Array,
     default: []
   }
});

const User = mongoose.model("User", userSchema);

app.post("/api/checkLogin", async (req, res) => {
  try {
    const { deviceId } = req.body;
    const existingGroup = await Group.findOne({ peopleList: deviceId });
    if (existingGroup) {
      res.status(200).json({ message: "User already in a group", existingGroup });
    } else {
      res.status(200).json({ message: "User not in a group" });
    }
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


app.post("/api/group/delete", async (req, res) => {
  try {
    const { groupId, deviceId } = req.body;
    // Ensure accurate input
    if (!groupId || !deviceId) {
      return res.status(400).json({ error: "groupId and deviceId are required" });
    }
    // Find and validate group
    const group = await Group.findOne({ groupId });

    if (!group) {
      return res.status(404).json({ error: "Group not found" });
    }

    // Creator only deletion
    if (group.creatorId !== deviceId) {
      return res.status(403).json({ error: "Only the creator can delete this hive" });
    }

    // Delete
    await Group.deleteOne({ groupId });

    res.json({ message: "Hive deleted successfully" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/leave", async (req, res) => {
  try {
    const { groupId, deviceId } = req.body;
    // Make sure the parameters are correct
    if (!groupId || !deviceId) {
      return res.status(400).json({ error: "groupId and deviceId required" });
    }

    // Check to see if the group to be deleted even exists
    const group = await Group.findOne({ groupId: groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });

    // Check if the user is even in the hive.
    if (!group.peopleList.includes(deviceId)) {
      return res.status(403).json({ error: "User is not part of this hive" });
    }

    // Delete the hive if the creator leaves
    if (group.creatorId === deviceId) {
      await Group.deleteOne({ groupId });
      return res.json({
        message: "Creator left — hive deleted for everyone",
        hiveDeleted: true
      });
    }

    // If a normal member leaves, just remove them from the list and save
    group.peopleList = group.peopleList.filter(id => id !== deviceId);
    await group.save();

    // Success!
    res.json({
      message: "User removed from hive",
      hiveDeleted: false,
      group
    });

  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


// route
app.post("/api/groups", async (req, res) => {
  try {
    const { groupName, creatorName, groupId, creatorId } = req.body;
    console.log("Request:",groupName, creatorName, groupId, creatorId)
//    if (!peopleList || peopleList.length === 0) {
//          return res.status(400).json({ error: "peopleList (deviceId) required" });
//    }
//    const deviceId = peopleList[0];

    // Create user profile if it doesn't exist
    let user = await User.findOne({ userId: creatorId });

    if (!user) {
      user = new User({
        userId: creatorId,
        name: creatorName,
        points: 0,
        profilePic: "",
      });

      await user.save();
    }

    // Optionally: check if user already has a group
    const existingGroup = await Group.findOne({ peopleList: creatorId });
    console.log("Existing group: ", existingGroup)
    if (existingGroup) {
      return res.status(200).json({ message: "User already in a group", existingGroup });
    }

    const group = new Group({ groupName, creatorName, groupId, creatorId, peopleList : [creatorId], groceries: [], chores: [] });
    await group.save();
    res.status(201).json({ message: "Group created", group });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/join", async (req, res) => {
  try {
    const { groupId, deviceId, userName } = req.body;
    console.log("Joining group with groupId: ", groupId, " deviceId: ", deviceId, " userName: ", userName)

    // Make sure all the required parameters are passed in
    if (!groupId || !deviceId || !userName) {
      return res.status(400).json({ error: "groupId, deviceId, and userName are required" });
    }
    console.log("Finding group")
    // Find the group to join
    const group = await Group.findOne({ groupId });
    if (!group) {
      console.log("Group not found")
      return res.status(404).json({ error: "Group not found" });
    }
    console.log("RIP")
    // Make sure the user isn't in a group already
    const existingGroupForUser = await Group.findOne({ peopleList: deviceId });
    if (existingGroupForUser && existingGroupForUser.groupId !== groupId) {
      return res.status(400).json({ error: "User already in another group" });
    }

    // Checks if the user already has a profile. If no, create one
    let user = await User.findOne({ userId: deviceId });
    if (!user) {
      user = new User({
        userId: deviceId,
        name: userName,
        points: 0,
        profilePic: "",
      });
      await user.save();
    }

    // Add user to this group's peopleList if not already present
    if (!group.peopleList.includes(deviceId)) {
      group.peopleList.push(deviceId);
      await group.save();
    }
    // Join group successful!
    return res.status(200).json({
      message: "Joined group successfully",
      group,
    });
  } catch (err) {
    return res.status(500).json({ error: err.message });
  }
});

app.post("/api/user/get", async (req, res) => {
    try {
        console.log("YES GETTING USER")
        const { userId } = req.body;
        const user = await User.findOne({ userId });
        if (!user) return res.status(404).json({ error: "User not found" });
        res.json({ user });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.post("/api/group/leaderboard", async (req, res) => {
  try {
    const { groupId, deviceId } = req.body;

    // Make sure groupid exists
    if (!groupId) {
      return res.status(400).json({ error: "groupId required" });
    }

    // Get the group
    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });

    // Auth check
    if (!group.peopleList.includes(deviceId)) {
        return res.status(403).json({ error: "User not authorized for this group" });
    }

    // Extract all userIds from peopleList
    const deviceIds = group.peopleList;

    // Get all users in the group
    const users = await User.find(
      { userId: { $in: deviceIds } },   // filter
      { name: 1, points: 1, _id: 1 }    // select fields
    ).sort({
      points: -1,  // higher points first
      _id: 1       // stable tie-breaker
    });
    // Create the leaderboard. Users already sorted.
    const leaderboard = users.map(u => ({
      name: u.name,
      points: u.points
    }));
    // Return the leaderboard
    res.json({ leaderboard });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/getUserNames", async (req, res) => {
  try {
    const { groupId, deviceId } = req.body;
    console.log("Get group user names with groupId: ", groupId)


    // Make sure the group id is valid
    if (!groupId) {
      return res.status(400).json({ error: "groupId is required" });
    }
    console.log("Finding group")
    // Make sure it exists
    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });
    console.log("Group Exists, getting deviceid")

    // AUTH CHECK
    if (!group.peopleList.includes(deviceId)) {
        return res.status(403).json({ error: "User not authorized for this group" });
    }

    // Get device IDs from the peopleList
    const deviceIds = group.peopleList;

    // Make sure people actually exist in the hive
    if (!deviceIds || deviceIds.length === 0) {
      return res.json({ names: [] });
    }
    console.log("More than 0 people exist!")
    // Find users and return only their names (_id: 0 comments out the default _id field)
    const users = await User.find(
      { userId: { $in: deviceIds } },
      { name: 1, _id: 0 }
    );

    // Extract the names into a list and return it
    const names = users.map(u => u.name);
    console.log("List of names", names)
    return res.json({ names });
  } catch (err) {
    console.error("GET MEMBER NAMES ERROR:", err);
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/get", async (req, res) => {
    try {
        console.log("YES GETTING GROUP.")
        const { groupId } = req.body;
        const group = await Group.findOne({ groupId });
        if (!group) return res.status(404).json({ error: "Group not found" });
        res.json({ group });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.post("/api/group/addGrocery", async (req, res) => {
    try {
        const { groupId, deviceId, name, description = "", quantity = 1, completed = false } = req.body;
        console.log("Adding grocery with groupId: ", groupId, " deviceId: ", deviceId, " name: ", name, " description: ", description)
        if (!groupId || !deviceId || !name) {
          return res.status(400).json({ error: "groupId, deviceId, and name are required" });
        }
        console.log("Finding Group")
        const group = await Group.findOne({ groupId });
        if (!group) return res.status(404).json({ error: "Group not found" });
        console.log("Checking AUTH")
        // AUTH CHECK
        if (!group.peopleList.includes(deviceId)) {
          return res.status(403).json({ error: "User not authorized for this group" });
        }
        console.log("PUSHING")
        group.groceries.push({
          name,
          description,
          quantity,
          completed
        });

        await group.save();
        res.status(200).json({ message: "Grocery added", groceries: group.groceries });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.post("/api/group/updateGrocery", async (req, res) => {
  try {
    console.log("Trying to update grocery with: ", req.body)
    const { groupId, deviceId, name, description = "", completed } = req.body;

    // Make sure the right fields are added
    if (!groupId || !deviceId || !name || typeof completed !== "boolean") {
      return res.status(400).json({ error: "groupId, deviceId, name, and completed(boolean) are required" });
    }
    // Make sure group exists
    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });
    console.log("GROUP FOUND, AUTH CHECKING FOR GROCERIES")
    // Make sure user is in group
    if (!group.peopleList.includes(deviceId)) {
      return res.status(403).json({ error: "Not authorized" });
    }
    console.log("Trying to find grocery with name: ", name, " description: ", description, " completed: ", completed)
    // Get the exact grocery from mongo
    const grocery = group.groceries.find(item =>
      item.name === name &&
      item.description === (description ?? "")
    );

    if (!grocery) {
      return res.status(404).json({ error: "Grocery item not found" });
    }
    console.log("FOUND GROCERY")
    // Update only the complete status and save
    grocery.completed = completed;

    await group.save();

    return res.json({
      message: "Grocery updated",
      groceries: group.groceries
    });

  } catch (err) {
    console.error("UPDATE GROCERY ERROR:", err);
    res.status(500).json({ error: err.message });
  }
});



app.post("/api/group/deleteGrocery", async (req, res) => {
  try {
    const { groupId, deviceId, name, description, quantity = 1, completed } = req.body;
    console.log("Deleting grcery with: ", groupId, deviceId, name, description, quantity, completed)
    // Make sure the required arguments are included
    if (!groupId || !deviceId || !name) {
      return res.status(400).json({ error: "Missing required fields" });
    }
    console.log("Getting Group")
    // Find group to delete from
    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });
    console.log("Auth check")
    // Make sure the user actually belongs to the group
    if (!group.peopleList.includes(deviceId)) {
      return res.status(403).json({ error: "Not authorized" });
    }
    console.log("Grocery Deletion")
    // Delete the grocery
    const before = group.groceries.length;

    group.groceries = group.groceries.filter(item =>
      !(
        item.name === name &&
        item.description === (description ?? "") &&
        item.quantity === quantity &&
        item.completed === completed
      )
    );
    // If no grocery was removed, then 404
    if (group.groceries.length === before) {
      return res.status(404).json({ error: "Grocery item not found" });
    }
    console.log("Grocery Deleted Successfully")
    // Save changes
    await group.save();
    res.json({ message: "Grocery deleted", groceries: group.groceries });

  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/addChore", async (req, res) => {
    try {
        const { groupId, deviceId, name, description = "", points = 0, status = 0, assignee = "" } = req.body;

        if (!groupId || !deviceId || !name) {
          return res.status(400).json({ error: "groupId, deviceId, and name are required" });
        }

        const group = await Group.findOne({ groupId });
        if (!group) return res.status(404).json({ error: "Group not found" });

        // AUTH CHECK
        if (!group.peopleList.includes(deviceId)) {
          return res.status(403).json({ error: "User not authorized for this group" });
        }

        group.chores.push({
          name,
          description,
          points,
          status,
          assignee
        });

        await group.save();
        res.status(200).json({ message: "Chore added", chores: group.chores });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.post("/api/user/completeChore", async (req, res) => {
  try {
    const { groupId, deviceId, choreName, description, points } = req.body;

    // Make sure we have all the relevant parameters
    if (!groupId || !deviceId || !choreName)
      return res.status(400).json({ error: "Missing required fields" });

    // Find the group to complete the chore from
    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });

    // Make sure the user belongs to the group
    if (!group.peopleList.includes(deviceId))
      return res.status(403).json({ error: "Not authorized" });

    // Find chore in the list
    const chore = group.chores.find(c =>
      c.name === choreName &&
      c.description === (description ?? "") &&
      c.points === points
    );

    // If the chore is not found, error
    if (!chore)
      return res.status(404).json({ error: "Chore not found" });

    // Update chore status (set to done = 2)
    chore.status = 2;
    chore.assignee = deviceId;
    // Save the chore being complete
    await group.save();

    // Find the user who completed this
    const user = await User.findOne({ userId: deviceId });

    if (!user)
      return res.status(404).json({ error: "User not found" });
    // Add the chore to the users completion list
    const epoch = Math.floor(Date.now() / 1000);

    user.choreRegister.push({
      name: choreName,
      description: description ?? "",
      points,
      completedAt: epoch
    });
    // Award the total points
    user.points += points;
    await user.save();

    return res.json({
      message: "Chore completed!",
      userPoints: user.points,
      choreRegister: user.choreRegister
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/updateChoreAssignee", async (req, res) => {
  try {
    const { groupId, deviceId, choreName, description = "", points, newAssignee } = req.body;
    console.log("Updating Chore Assignee with: ", groupId, deviceId, choreName, description, points, newAssignee)
    // Validate input
    if (!groupId || !deviceId || !choreName || points === undefined) {
      return res.status(400).json({ error: "Missing required fields" });
    }

    // Make sure gorup exists
    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });

    // Make sure user belongs in group
    if (!group.peopleList.includes(deviceId)) {
      return res.status(403).json({ error: "Not authorized" });
    }

    // Find chore, uniqueness is defined by name, desc, points
    const chore = group.chores.find(c =>
      c.name === choreName &&
      c.description === description &&
      c.points === points
    );
    console.log("Chore found: ", chore)
    if (!chore)
      return res.status(404).json({ error: "Chore not found" });

    // Update assignee and save
    chore.assignee = newAssignee;
    console.log("updated chore: ", chore)
//    await chore.save();
//    console.log("saved chore")

    await group.save();
    console.log("Saved")

    return res.json({
      message: "Chore assignee updated",
      chores: group.chores
    });
  } catch (err) {
    console.error("UPDATE ASSIGNEE ERROR:", err);
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/updateChoreStatus", async (req, res) => {
  try {
    const { groupId, deviceId, choreName, description = "", points, newStatus } = req.body;
    // Make sure right parameters are passed in
    console.log("Updating Chore Status with: ", groupId, deviceId, choreName, description, points, newStatus)
    if (!groupId || !deviceId || !choreName || points === undefined || newStatus === undefined) {
      return res.status(400).json({ error: "Missing required fields" });
    }
    // Ensure group exists
    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });
    // AUTH CHECK FOR DEVICE
    if (!group.peopleList.includes(deviceId)) {
      return res.status(403).json({ error: "Not authorized" });
    }
    // Find the chore
    const chore = group.chores.find(c =>
      c.name === choreName &&
      c.description === description &&
      c.points === points
    );

    if (!chore) {
      return res.status(404).json({ error: "Chore not found" });
    }

    // update status of the chore and save
    chore.status = newStatus;

    await group.save();

    return res.json({
      message: "Chore status updated",
      chores: group.chores
    });

  } catch (err) {
    console.error("UPDATE STATUS ERROR:", err);
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/updateChore", async (req, res) => {
  try {
    const { groupId, deviceId, name, description = "", points, newAssignee, newStatus } = req.body;

    console.log("Updating chore with:", groupId, deviceId, name, description, points, newAssignee, newStatus);

    // Make sure it valid request
    if (!groupId || !deviceId || !name || points === undefined) {
      return res.status(400).json({ error: "Missing required fields" });
    }

    if (newStatus < 0 || newStatus > 2 || !Number.isInteger(newStatus)) {
      return res.status(400).json({ error: "Invalid status" });
    }

    // Make sure group exists
    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });

    // Auth check on deviceID
    if (!group.peopleList.includes(deviceId)) {
      return res.status(403).json({ error: "Not authorized" });
    }

    // Find the chore by unique field
    const chore = group.chores.find(c =>
      c.name === name &&
      c.description === description &&
      c.points === points
    );

    if (!chore) {
      return res.status(404).json({ error: "Chore not found" });
    }

    console.log("Found chore:", chore);

    // Apply updates and save
    chore.assignee = newAssignee;
    chore.status = newStatus;

    console.log("Updated chore:", chore);

    // Save group
    await group.save();

    return res.json({
      message: "Chore updated successfully",
      chores: group.chores
    });

  } catch (err) {
    console.error("UPDATE CHORE ERROR:", err);
    return res.status(500).json({ error: err.message });
  }
});



app.post("/api/group/deleteChore", async (req, res) => {
  try {
    const { groupId, deviceId, choreName, description, points, status, assignee } = req.body;
    // Makes sure we have valid input parameters
    if (!groupId || !deviceId || !choreName || points === undefined) {
      return res.status(400).json({ error: "Missing fields" });
    }
    // Status can only be 0, 1, or 2. If not, return error
    if (status < 0 || status > 2 || !Number.isInteger(status)) {
          return res.status(400).json({ error: "Status not allowed" });
        }


    // Make sure the group exists and that the requester actually belongs to the group
    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });

    if (!group.peopleList.includes(deviceId)) {
      return res.status(403).json({ error: "Not authorized" });
    }

    // Attempt to remove the chore
    const before = group.chores.length;
    group.chores = group.chores.filter(chore =>
      !(
        chore.name === choreName &&
        chore.description === (description ?? "") &&
        chore.points === points &&
        chore.status === status &&
        chore.assignee === assignee
      )
    );

    // If the removal failed (because it couldn't find and remove the chore), it errors
    if (group.chores.length === before) {
      return res.status(404).json({ error: "Chore not found" });
    }

    // Save the new group without the chore
    await group.save();
    res.json({ message: "Chore deleted", chores: group.chores });

  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});


app.post("/api/user/updateProfilePic", async (req, res) => {
  try {
    const { deviceId, profilePic } = req.body;
    console.log("Updating profile picture with deviceId: ", deviceId, " profilePic: ", profilePic);
    // Make sure valid request
    if (!deviceId || typeof profilePic !== "string") {
      return res.status(400).json({ error: "deviceId and profilePic are required" });
    }

    // Make sure user exists
    const user = await User.findOne({ userId: deviceId });
    if (!user) return res.status(404).json({ error: "User not found" });

    // Update profile picture and save
    user.profilePic = profilePic;
    await user.save();

    return res.json({
      message: "Profile picture updated successfully",
      profilePic: user.profilePic
    });

  } catch (err) {
    console.error("PROFILE PIC UPDATE ERROR:", err);
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/updateName", async (req, res) => {
    try {
        console.log("Changing Group Name!")
        const { groupId, deviceId, newName } = req.body;

        // Validate that it is a valid request
        if (!groupId || !deviceId || !newName)
          return res.status(400).json({ error: "Missing fields" });

        // Find the requested group
        const group = await Group.findOne({ groupId });
        if (!group) return res.status(404).json({ error: "Group not found" });

        // Make sure the user is in the group
        if (!group.peopleList.includes(deviceId))
          return res.status(403).json({ error: "Not authorized" });

        group.groupName = newName;
        await group.save();

        res.json({ message: "Group name updated", group });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.post("/api/user/updateName", async (req, res) => {
    try {
        console.log("Changing Username!")
        const { userId, newName } = req.body;

        // Make sure valid parameters passed in
        if (!userId || !newName)
          return res.status(400).json({ error: "Missing fields" });

        // Make sure the user actually exists
        const user = await User.findOne({ userId });
        if (!user) return res.status(404).json({ error: "User not found" });

        // Set and save the new name
        user.name = newName;
        await user.save();

        res.json({ message: "Username updated", user });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});




// start server
app.listen(3000, () => console.log("Server running on port 3000"));