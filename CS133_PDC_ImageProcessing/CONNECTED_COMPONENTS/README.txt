

How to execute connected component labelling
We used 2 images:
Small size - 2048x2048 - snowmass_2048.bmp
Big Size - 5096x5096 - snowmass_5096.bmp
1. Sequential Code
   For compiling: gcc -o CCLSequential bmpfile.c connectedComponentSequential.c -lm
   For executing:
   ./CCLSequential [image_filename]
   ./CCLSequential snowmass_2048.bmp
2. Parallel Code
   For compiling: gcc -fopenmp -o CCLParallel bmpfile.c connectedComponentParallel.c -lm
   For executing:
   ./CCLParallel [image_filename] [no_threads]
   ./CCLParallel snowmass_5096.bmp 8
   
  -----Please include snowmass_2048 and snowmass_5096.bmp files