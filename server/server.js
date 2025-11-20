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
      }
    ],

    chores: [
      {
        name: { type: String, required: true },
        description: { type: String, default: "" },
        points: { type: Number, default: 0 }
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

// route
app.post("/api/groups", async (req, res) => {
  try {
    const { groupName, creatorName, groupId, creatorId } = req.body;
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
    const { groupId, deviceId, name, description = "", quantity = 1 } = req.body;

    if (!groupId || !deviceId || !name) {
      return res.status(400).json({ error: "groupId, deviceId, and name are required" });
    }

    const group = await Group.findOne({ groupId });
    if (!group) return res.status(404).json({ error: "Group not found" });

    // AUTH CHECK
    if (!group.peopleList.includes(deviceId)) {
      return res.status(403).json({ error: "User not authorized for this group" });
    }

    group.groceries.push({
      name,
      description,
      quantity
    });

    await group.save();
    res.status(200).json({ message: "Grocery added", groceries: group.groceries });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/group/addChore", async (req, res) => {
  try {
    const { groupId, deviceId, name, description = "", points = 0 } = req.body;

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
      points
    });

    await group.save();
    res.status(200).json({ message: "Chore added", chores: group.chores });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});




// start server
app.listen(3000, () => console.log("Server running on port 3000"));