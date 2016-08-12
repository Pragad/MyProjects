
# ---------------------------------------------------------------
# 4. CBR
# 
# CBR simulates a constant bit rate generator.  In order to use CBR, the
# following format is needed:
# 
#     CBR <src> <dest> <items to send> <item size> 
#         <interval> <start time> <end time>
# 
# where
# 
#     <src> is the client node.
#     <dest> is the server node.
#     <items to send> is how many application layer items to send.
#     <item size> is size of each application layer item.
#     <interval> is the interdeparture time between the application layer items.
#     <start time> is when to start CBR during the simulation.
#     <end time> is when to terminate CBR during the simulation.
# 
# If <items to send> is set to 0, CBR will run until the specified
# <end time> or until the end of the simuation, which ever comes first.  
# If <end time> is set to 0, CBR will run until all <items to send>
# is transmitted or until the end of simulation, which ever comes first.
# If <items to send> and <end time> are both greater than 0, CBR will
# will run until either <items to send> is done, <end time> is reached, or 
# the simulation ends, which ever comes first.
# 
# EXAMPLE:
# 
#     a) CBR 1 2 10 1460 1S 0S 600S
# 
#        Node 1 sends node 2 ten items of 1460B each at the start of the 
#        simulation up to 600 seconds into the simulation.  The interdeparture
#        time for each item is 1 second.  If the ten items are sent before 
#        600 seconds elapsed, no other items are sent.
# 
#     b) CBR 1 2 0 1460 1S 0S 600S
# 
#        Node 1 continuously sends node 2 items of 1460B each at the start of
#        the simulation up to 600 seconds into the simulation.
#        The interdeparture time for each item is 1 second.
#       
#     c) CBR 1 2 0 1460 1S 0S 0S
# 
#        Node 1 continuously sends node 2 items of 1460B each at the start of
#        the simulation up to the end of the simulation.
#        The interdeparture time for each item is 1 second.
#
#


CBR 41 50 10 512 3S 25S 0S PRECEDENCE 1
CBR 44 50 10 512 3S 5S 0S PRECEDENCE 1
CBR 47 50 10 512 3S 10S 0S PRECEDENCE 1
CBR 26 50 10 512 3S 15S 0S PRECEDENCE 1
CBR  3 50 10 512 3S 20S 0S PRECEDENCE 1
CBR  6 50 10 512 3S 25S 0S PRECEDENCE 1
CBR  9 50 10 512 3S 1S 0S PRECEDENCE 1
CBR 12 50 10 512 3S 7S 0S PRECEDENCE 1
CBR 15 50 10 512 3S 12S 0S PRECEDENCE 1
CBR 18 50 10 512 3S 17S 0S PRECEDENCE 1
CBR 21 50 10 512 3S 22S 0S PRECEDENCE 1
