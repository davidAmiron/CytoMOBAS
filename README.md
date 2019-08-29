# CytoMoBaS
An implementation of the MoBaS clustering algorithm as a Cytoscape Application.

## Summary
This software was written by David Miron under Marzieh Ayati in the lab of Mehmet Koyuturk. The MoBaS algorithm
was developed by Marzieh Ayati ([Original Paper](www.google.com)). This software is an implementation
of this algorithm as a Cytoscape application, in order to make the algorithm more accessible.

## Usage
1. Select your network in the Control Panel
2. Select Apps > MoBaS
3. Select parameters and click submit
   - For details on parameters, see the application paper
4. Open the results panel > MoBaS Results
   - Enter a project name to select a different project
   - Click on a row to highlight the nodes in that subnetwork

## Notes
- Subnetworks found with 2 or less nodes are not written.
- Subnetworks and score data are saved in [user home]/CytoscapeConfiguration/app-data/MoBaS/[project name]


## Known Bugs
- An error appears when the size of the network is too small (less than around 15 nodes)
- If the user clicks on one row, and then another, the results of the previous highlight will remain highlighted as well
