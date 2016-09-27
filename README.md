# Last-One-Out
Android driving survival game, featuring random city generation (and zombies, lots of zombies).

Unfortunately the whole project is too large to upload directly, but soon I will provide a link on my portfolio website if someone wanted the whole project.  

This was a final project for an Android app development class in college.  I used Java with the Libgdx library to produce a simple 3d world for gameplay.  I have yet to add a lot of the game-like features, so it is still relatively simple, but a great framework for expansion. In other words, most of the hard work is done.  

On game start it creates a randomly generated world for exploration.  It first populates a number of cities, then connects those cities with roads if they are close enough.  Then it places houses and orientates them relative to the road.  It then creates random drop-off locations around the edge of the map and creates roads from them to the nearest city.  Finally, it creates rocks, trees, bushes, and zombies to populate the world.

The goal is to follow the minimap to the highlighted houses and collect survivors.  Once your car is full of passengers, the minimap will change to show the dropoff locations.  Once all survivors have been collected, the stage is over and you score points based on the amount of damage your vehicle has taken, the amount of time it took, and the amount of fuel left in the vehicle.

Currently there are 5 stages per level, which get harder by adding more houses and more survivors to collect.  Since your car can only take a limited amount of damage and has a set amount of fuel, it naturally become more difficult this way. 

Main Classes
 - MyGdxGame: (Terrible name I know) This method calls to the worldGenerator class to create the world.  Then it manages gameplay every frame: moving enemies, moving and rotating player, determining game states, etc.
 - WorldGenerator:  Creates the randomly generated levels.
 - Input(BulletInputProcessor, GetInput, RayTesting): These three classes detect input during gameplay and on menu screens.  Raytesting is currently not really used, but will be to add a gun firing mechanic.
 - Game entity classes (City, Zombie, GameObject): These are several classes to manage instance data throughout the world.
  
Coming eventually:
- Saving level progress and high scores.
- Infinte levels, by incrementing the random generator seed for each level (with 5 random stages each).
- Sound effects, background music and collision sound effects.
- Adding people running from their houses to your car, you currently fill a meter which isn't very realistic.
- Options menu, currently there is a options button but it doesn't work yet.
- Particle effects for collisions.
- For main class, change name cause it is silly, and refactor by adding getters/setters

You can view a video of the first level at https://www.youtube.com/watch?v=80Tu0zkfKD4
