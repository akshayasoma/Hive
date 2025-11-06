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
  peopleList: [String]
});

const Group = mongoose.model("Group", groupSchema);

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
    const { groupName, creatorName, groupId, peopleList } = req.body;
    if (!peopleList || peopleList.length === 0) {
          return res.status(400).json({ error: "peopleList (deviceId) required" });
    }
    const deviceId = peopleList[0];

    // Optionally: check if user already has a group
    const existingGroup = await Group.findOne({ peopleList: deviceId });
    console.log("Existing group: ", existingGroup)
    if (existingGroup) {
      return res.status(200).json({ message: "User already in a group", existingGroup });
    }

    const group = new Group({ groupName, creatorName, groupId, peopleList });
    await group.save();
    res.status(201).json({ message: "Group created", group });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// start server
app.listen(3000, () => console.log("Server running on port 3000"));