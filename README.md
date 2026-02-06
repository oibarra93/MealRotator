<h1>Meal Rotator</h1>

<p>
Meal&nbsp;Rotator is an Android application written in Kotlin for planning weekly meals and
producing a consolidated grocery list.  It uses a local Room database to store meals and
their ingredients and provides screens for adding/editing meals, scheduling them across
the week and generating shopping lists.
</p>

<h2>Features</h2>
<ul>
  <li><strong>Add &amp; edit meals.</strong>  A dedicated screen allows users to create a new meal or
    edit an existing one.  It captures a name, preparation instructions and a list of
    ingredients.  Ingredient lines entered by the user are split on newlines and
    converted into ingredient objects before being saved to the database.</li>
  <li><strong>Weekly scheduling.</strong>  The Schedule screen can generate a weekly plan by
    randomly shuffling available meals and assigning them to each day of the week.  Users
    can also “pin” a meal to a specific day to prevent it being replaced when
    re‑generating the schedule.</li>
  <li><strong>Grocery list generation.</strong>  When you have a schedule, the app can build a
    grocery list by reading the ingredients from each scheduled meal, grouping them
    by ingredient name and concatenating their amounts.  This produces a simple text
    list that can be copied into a notes app or used for shopping.</li>
  <li><strong>List of meals.</strong>  The main screen displays all meals stored in the
    database using a RecyclerView.  A floating action button opens the Add/Edit Meal
    screen, and navigation options let you open the scheduling view or generate a
    grocery list for the current week.</li>
</ul>

<h2>Getting&nbsp;started</h2>
<p>
Meal&nbsp;Rotator is an Android Studio project.  Clone the repository and open it in
Android Studio.  Build and run the app on a device or emulator.  Use the
“Add” button on the main screen to enter your meals, then open the Schedule
screen to create a weekly plan.  Once you have a schedule, use the “Grocery
List” option to generate a consolidated shopping list.
</p>
