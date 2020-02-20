# Cadherin
Cell-Cell Adhesion
Here is the project of modelling the cell-cell adhesion through Cadherin 

2020.02.19
1. After testing different timesteps, I choose to use the timestep = 1e-5 s. This can put all rate constant into
a reasonable range to indicate the probability of change of the states.
2. still we apply the cut-off distance as 1.5 nm if the distance less than 1,5 then it will form x-dimer or s-dimer 
accordingly to the probability?  [still feel this part is not clear!]

if we generate a random number and compare with the probability number. The the small probability will definitely 
satisfy the larger one. This will cause the problem. 

Another, if we assume that all monomers form the x-dimer first then how do we define the state to change to S-dimer, this
will be problem in this method.
