#include "StdAfx.h"
#include "dbn.h"
/*
 * Author: Sari Haj Hussein
 */
/** public methods **/
void dbn::info()
{
	cout << "General DBN Information" << endl;
	cout << "-----------------------" << endl; 
	cout << "Default BN Algorithm: " << this->network->GetDefaultBNAlgorithm() << endl;
	cout << "Default HBN Algorithm: " << this->network->GetDefaultHBNAlgorithm() << endl;
	cout << "Default ID Algorithm: " << this->network->GetDefaultIDAlgorithm() << endl;
	cout << "Depth Of Net: " << this->network->GetDepthOfNet() << endl;
	cout << "Max Temporal Order: " << this->network->GetMaxTemporalOrder() << endl;
	cout << "Network Flags: " << this->network->GetNetworkFlags() << endl;
	cout << "Number Of Cases: " << this->network->GetNumberOfCases() << endl;
	cout << "Number Of Discretization Samples: " << this->network->GetNumberOfDiscretizationSamples() << endl;
	cout << "Number Of Nodes: " << this->network->GetNumberOfNodes() << endl;
	cout << "Number Of Samples: " << this->network->GetNumberOfSamples() << endl;
	cout << "Number Of Slices: " << this->network->GetNumberOfSlices() << endl;
	cout << "Show As: " << this->network->GetShowAs() << endl << endl;;

    DSL_node* location = this->network->GetNode(0);
    DSL_node* reader = this->network->GetNode(1);

    // indices and names of each node
    cout << "Indices and names of nodes in the model" << endl;
    cout << "---------------------------------------" << endl;
    cout << "Index: 0 " << "\t Name: " << location->GetId() << endl;
    cout << "Index: 1 " << "\t Name: " << reader->GetId() << endl << endl;

    // indices and names of each nodes outcome
    int  locationOutcomesSize = location->Definition()->GetNumberOfOutcomes();
    int  readerOutcomesSize = reader->Definition()->GetNumberOfOutcomes();
    DSL_idArray* locationOutcomes = location->Definition()->GetOutcomesNames();
    DSL_idArray* readerOutcomes = reader->Definition()->GetOutcomesNames();

    cout << "Indices and names for outcomes in the location node" << endl;
    cout << "---------------------------------------------------" << endl;
    for(int index = 0; index < locationOutcomesSize; index++)
        cout << "Index: " << index << "\t Name: " << locationOutcomes[0][index] << endl;

    cout << endl << "Indices and names for outcomes in the reader node" << endl;
    cout << "-------------------------------------------------" << endl;
    for(int index = 0; index < readerOutcomesSize; index++)
        cout << "Index: " << index << "\t Name: " << readerOutcomes[0][index] << endl;
}

void dbn::updateBeliefs()
{
	this->network->UpdateBeliefs();
}

void dbn::setNumberOfSlices(int slices)
{
	this->network->SetNumberOfSlices(slices);
}

vector<string> dbn::getLocationOutcomes()
{
	vector<string> locations;
	DSL_node* location = this->network->GetNode(0);
	int  locationOutcomesSize = location->Definition()->GetNumberOfOutcomes();
	DSL_idArray* locationOutcomes = location->Definition()->GetOutcomesNames();
    for(int index = 0; index < locationOutcomesSize; index++)
		locations.push_back(locationOutcomes[0][index]);
	return locations;
}

vector<double> dbn::getProbabilityDistribution(int nodeIndex, int timeSlice)
{
    vector<double> probabilityDistribution;

    // extracts all the probabilities from the locations node
    unsigned int numberOfOutcomes = this->network->GetNode(nodeIndex)->Definition()->GetNumberOfOutcomes();
    DSL_doubleArray dArray = this->network->GetNode(nodeIndex)->Value()->GetMatrix()->GetItems();
 
	// the values for a node is saved as one big double array, so we have to extract only a small piece
    unsigned int timeSliceStart = timeSlice*numberOfOutcomes;
    unsigned int timeSliceEnd = timeSlice*numberOfOutcomes+numberOfOutcomes;

	if (dArray.GetSize() == 18)
		for(unsigned int index = timeSliceStart; index < timeSliceEnd; index++)
			probabilityDistribution.push_back(dArray[index]);
	else // if the DBN fails for whatever reason in evoling a belief, then assume a uniform distribution
		for(unsigned int index = 0; index < 6; index++)
			probabilityDistribution.push_back(1.0/6.0);

    return probabilityDistribution;
}

void dbn::setTemporalVirtualEvidence(int nodeIndex, int timeSlice, vector<double> evidence)
{
	DSL_node *node = this->network->GetNode(nodeIndex);
	node->Value()->SetTemporalEvidence(timeSlice, evidence);
}

/** constructor **/
dbn::dbn(string filePath, unsigned int fileType, int theAlgorithm)
{
	// ensures that they won't be deleted by the destructor if the construction fails
	this->network = NULL;

	this->network = new DSL_network(); 
	this->network->ReadFile(filePath.c_str(), fileType);
	this->network->SetDefaultBNAlgorithm(theAlgorithm);

	// check the structure of the imported model to make sure that it was properly read
	if(!this->verifyStructure())
	{
		cout << "ERROR: the model imported does not have a known structure, exiting." << endl;
		delete this->network;
		exit(-1);
	}
}

/** destructor **/
dbn::~dbn()
{
	if (this->network != NULL)
		delete this->network;
}

/** private methods **/
bool dbn::verifyStructure()
{
	unsigned int numberOfNodes = this->network->GetNumberOfNodes();

	// our model contains two nodes Location and Reader, so everything else is unacceptable
	if(numberOfNodes != 2)
		return false;

	// check the names of the two nodes, and if something is wrong return false
	if((this->network->GetNode(0)->GetId()[0] != 'L') || (this->network->GetNode(1)->GetId()[0] != 'R'))
		return false;

	// check the size of the definition matrix on each node -> accessing missing nodes could be a problem 
	unsigned int locationMatrixSize = this->network->GetNode(0)->Definition()->GetMatrix()->GetSize();
	unsigned int readerMatrixSize = this->network->GetNode(1)->Definition()->GetMatrix()->GetSize();

	if((locationMatrixSize != 6) || (readerMatrixSize != 30))
		return false;

	// everything seems to be OK
	return true;
}