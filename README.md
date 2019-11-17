# OxHack
shark only territory
## Inspiration
- We couldn't think of anything else :(

## What it does
- A Pigment of Your Imagination allows users to select images, each of which are then converted into a colouring stencil for the user to fill in.
- The conversion process involves firstly applying a clustering algorithm to compress the original uploaded image.
- Upon completion, edge detection is run on the code in order to section the image into different coloured regions.
- A noise-reduction process was then applied in order to minimise any unwanted clustering.

## Construction
- The app was built using Android Studio, and a mix of Java and Kotlin code.

## Challenges we ran into
- Everything. Legitimately, everything.

## Accomplishments that we're proud of
- This was the first hackathon for three of us, and we're quite happy with how much we've progressed considering how little experience in Kotlin and Java we had.
- Fixing 84 merge conflicts in Github at 4.30am >:( 

## What we learned
- How to use Android Studio, and how to program in two new languages!

## What's next for A Pigment of Your Imagination
- We can improve the speed of image processing by using a web server instead of processing the image on our devices. Also, including a cloud server that can house pre-made stencils (e.g. Microsoft Azure).