#pragma once

#include <iostream>
#include <string>

#include "smile/smile.h"

using namespace std;
/*
 * Author: Sari Haj Hussein
 */
class dbn
{
public:
	/** public methods **/
	void info();
	void updateBeliefs();
	vector<string> getLocationOutcomes();
	void setNumberOfSlices(int slices);
	vector<double> getProbabilityDistribution(int nodeIndex, int timeSlice);
	void setTemporalVirtualEvidence(int nodeIndex, int timeSlice, vector<double> evidence);

	/** constructor **/
	dbn(string filePath, unsigned int fileType, int theAlgorithm);
	/** destructor **/
	virtual ~dbn();
private:
	/** private methods **/
	bool verifyStructure();
	/** instance variables **/
	DSL_network *network;
};
