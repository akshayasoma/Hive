import express from "express";
import mongoose from "mongoose";
import cors from "cors";
import dotenv from "dotenv";

dotenv.config();

const app = express();
app.use(express.json());
app.use(cors());

// connect to MongoDB Atlas
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
  profilePicture: {
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
        profilePicture: "",
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
        profilePicture: "",
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