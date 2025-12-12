# Hive
CS 407 Course Project

We will Bee the best :D

<br>

## Bookkeeping<br>
### Week 1

#### Roles:
Observer: N/A Getting Started<br>
Coordinator: N/A Getting Started<br>

#### Accomplishments:
Kavya: Initial set up of the app (color,theme etc), Login, Create, Join and Home Screen <br>
Glenn: Initialized the database and setup a connection between Hive and MongoDB. <br>
Pranav: Provided inital base code.<br>
Akshaya: Initial setup of Chores, Groceries, and Recipe screens. Includes basic hardcoded UI and navigation <br>

<br>

### Week 2

#### Roles:
Observer: Pranav<br>
Coordinator: Kavya<br>

#### Accomplishments:
Kavya: Made the settings screen (hardcoded the grpName, grpId and username). In app light and dark mode. Android share screen/sheet to share grpId. Custom Profile Icons added.<br>
Glenn:Setup a backend server and established a connection from Android to NodeJS. Updated the manifest to allow for internet access and setup the backend server to allow for requests. Android -> Node -> MongoDB pipeline. Made the sign-up button actually create a new group on MongoDB. Set-up initial leaderboard screen<br>
Pranav: Created the initial Perplexity API and camera vision integration.<br>
Akshaya: Created the 'Add' pop-up boxes for Chores, Groceries, and Recipes. Pop-up adds cards to its respective screens. Modified leaderboard ui screen to show better visual hierarchy of the users<br>

<br>

### Week 3

#### Roles:
Observer: Pranav<br>
Coordinator: Kavya<br>

#### Accomplishments:
Kavya:Added the info box content and its fade in fade out animation.<br>
Glenn:Re-did the initial MongoDB Schema for Hive. Added a schema to store user data and have the data connected by their deviceid. Added a sharedPreferences Datastore for groupid and username. Settings are now able to populate username, groupname, and groupid without being hardcoded. Adjusted the create button such that it creates a Hive with the new schema and creates a User data if the deviceid isn't already registered.<br>
Pranav: Added text-only AI services and refined the prompt for usage in the app. Also added some optimizations to the build process.<br>
Akshaya: Reworked the RecipeScreens UI completely. Can add ingredients, click "Find Recipe," which now shows (hardcoded) recipes. Each recipe card has Dish name, difficulty level, and duration, which you can sort by. Clicking on each card, opens a pop-up with the Dish's ingredient list with quantities and the instructions.<br>

<br>

### Week 4

#### Roles:
Observer: Akshaya<br>
Coordinator: Glenn<br>

#### Accomplishments:
Kavya: Completely redesigned the homescreen of the app. First created the design on Canva and then implemented it in the app. Added the custom font Cooper BT Bold to improve the visual appeal. Created a bee flying around a hive animation using Lottie, added the required dependency, and incorporated the animation on the Create and Join screens.<br>
Glenn: Added in new settings integrations. Deleting hive and changing hive+user settings now work.<br>
Pranav:Worked on recipe creation systems and worked on the text only interface for the recipe generation. Still in progress with prompting and enhancements.<br>
Akshaya:All pop-ups/alert dialog boxes are color coded with the theme. This includes Chores, Groceries, Recipe, and Settings. Chore cards and Grocery cards are now expandable when clicked to see description. Grocery card fades when the checkbox is checked to visually see completion.<br>

<br>

### Week 5/6 (Thanksgiving Break)

#### Roles:
Observer: Akshaya<br>
Coordinator: Glenn<br>

#### Accomplishments:
Kavya: Implemented the shake animation for delete mode, swiping left animation to delete and color gradient effect on swipe left for chores and grocery screen. Changed the Font throughout all screens. Added flying bee animation to the login screen. Fixed the text wrapping of the chores and grocery cards. Updated the camera UI.<br>
Glenn:Add + Remove chores and groceries in DB. Updated front end callback logic. Added Join  + Leave hive.<br>
Pranav: AI for recipe generation has been successfully implemented. Also modified the camera to detect ingredients and send them over to the recipe generator. Also added periodic notifications so that the user is aware of changes to the hive in name, chores, or groceries. Added a method to manually invoke it too.<br>
Akshaya: Toast messages/notifications show up when users add/delete chores or groceries and also when you change assign a user or if you change status of a chore. Created a button that allows users to assign a member to a chore and to change the status of said chore. Once completed, the chore will have a strike and fade out.<br>

<br>

### Week 7

#### Roles:
Observer: Everyone<br>
Coordinator: Everyone<br>

#### Accomplishments:
Kavya: Added finer datails for the app to be more cohesive. Added error statements visible to the user along with its logic. Fixed the in app dark/light mode UI errors.<br>
Glenn:Added in helper methods for less raw and easier access to data and helped query ui information<br>
Pranav: Made the app operational again and fixed all the bugs associated with all core app services(Leaderboard, database connection, chores, and grocery).<br>
Akshaya: Made leaderboard work with all user. Allowed assigned users to persist withing the database for each chore. Profile pics are saved within settings and show up for users on leaderboard. Points are tallied after each chore completion and reflected in leaderboard.<br>
