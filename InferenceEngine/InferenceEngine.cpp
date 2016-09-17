// InferenceEngine.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "dbn.h"
#include "probrecord.h"
#include "smile/constants.h"
#include <vector>
#include <iomanip>
#include <string>
#include <sstream>
#include <iterator>
#include <hash_map>

using namespace std;
/*
 * Author: Sari Haj Hussein
 */
class utilityclass
{
public:
	template <class T>
	static void printVector(vector<T> v)
	{
		for (vector<T>::const_iterator it = v.begin(); it != v.end(); ++it)
			cout << *it << " ";
		cout << endl;
	}

	static vector<char*> tokenize(char* str, char* sep)
	{
		/*vector<char*> v;
		char* token = NULL;
		char* next_token = NULL;
		token = strtok_s(str, sep, &next_token);
		while(token != NULL)
		{
			v.push_back(token);
			token = strtok_s(NULL, sep, &next_token);
		}
		return v;*/
		vector<char*> v;
		char* token = strtok(str, sep);
		while(token != NULL)
		{
			v.push_back(token);
			token = strtok(NULL, sep);
		}
		return v;
	}

	static vector<string> tokenize(string str, char sep)
	{
		vector<string> v;
		replace(str.begin(), str.end(), sep, ' ');
		istringstream iss(str);
		copy(istream_iterator<string>(iss),
			istream_iterator<string>(),
			back_inserter<vector<string> >(v));
		return v;
	}

	static time_t mktimeUTC(struct tm* timeinfo)
	{
		// enter UTC mode
		char* oldTZ = getenv("TZ");
		_putenv("TZ=UTC");
		_tzset();
		time_t ret = mktime ( timeinfo );
		// restore previous TZ
		if(oldTZ == NULL)
		{
			_putenv("TZ=");
		}
		else
		{
			char buff[255];
			sprintf(buff,"TZ=%s",oldTZ);
			_putenv(buff);
		}
		_tzset();
		return ret;
	}

	static time_t getUnixTime(string time_str) {
		time_t loctime;
		time(&loctime);

		struct tm *given_time;
		time_str = time_str.substr(0, time_str.find_first_of('.'));

		replace(time_str.begin(), time_str.end(), ':', ',');
		replace(time_str.begin(), time_str.end(), '-', ',');
		replace(time_str.begin(), time_str.end(), '/', ',');
		replace(time_str.begin(), time_str.end(), ' ', ',');
		replace(time_str.begin(), time_str.end(), '!', ','); // since the time value contains '!'

		given_time = localtime(&loctime);
		vector<string> trecord = tokenize(time_str, ',');

		given_time->tm_year = atoi(trecord.at(0).c_str()) - 1900;
		given_time->tm_mon  = atoi(trecord.at(1).c_str()) - 1;
		given_time->tm_mday = atoi(trecord.at(2).c_str());
		given_time->tm_hour = atoi(trecord.at(3).c_str());
		given_time->tm_min  = atoi(trecord.at(4).c_str());
		given_time->tm_sec  = atoi(trecord.at(5).c_str());

		return mktimeUTC(given_time);
	}

	static probrecord buildProbRecord(vector<char*> fields)
	{
		string licensePlate = fields[0];
		string probLocation = fields[1];
		string sTime = fields[2];
		replace(sTime.begin(), sTime.end(), '!', ' ');
		string eTime = fields[3];
		replace(eTime.begin(), eTime.end(), '!', ' ');
		__int64 sTimeUnix = static_cast<__int64> (getUnixTime(sTime));
		__int64 eTimeUnix = static_cast<__int64> (getUnixTime(eTime));
		return probrecord(licensePlate, probLocation, sTime, eTime, sTimeUnix, eTimeUnix);
	}

	static vector<probrecord> getProbRecords(char* arg)
	{
		vector<probrecord> precs;
		vector<char*> records = tokenize(arg, "#"); // records are separated via "#"
		for (vector<char*>::const_iterator it = records.begin(); it != records.end(); ++it)
		{
			vector<char*> fields = tokenize(*it, ";"); // fields are separated via ";"
			probrecord prec = buildProbRecord(fields);
			precs.push_back(prec);
		}
		return precs;
	}

	static int getNumberOfSlices(probrecord prec0, probrecord prec1, char* arg)
	{
		int interval = atoi(arg);
		int num = 2; // count prec0 and prec1
		int start = prec0.geteTimeUnix() + interval;
		int end = prec1.getsTimeUnix() - 1;
		if (start <= end)
			num++;
		return num;
	}

	static vector<double> getEvidence(probrecord prec, vector<string> locations)
	{
		string probLocation = prec.getProbLocation();
		vector<string> tokens = tokenize(probLocation, ',');
		hash_map<string, double> probLocationMap;
		hash_map<string, double>::iterator mapIterator;

		for (vector<string>::const_iterator it = locations.begin(); it != locations.end(); ++it)
			probLocationMap[*it] = 0; // initialize all the probabilities in the hash map to zero

		for (vector<string>::const_iterator it = tokens.begin(); it != tokens.end(); ++it)
		{
			vector<string> subtokens = tokenize(*it, ':');
			string &subtoken0 = subtokens.at(0);
			string &subtoken1 = subtokens.at(1);
			const char* c_subtoken1 = subtoken1.c_str();
			probLocationMap[subtoken0] = atof(c_subtoken1); // attach the probabilities from the probrecord
		}

		vector<double> evidence;
		for (vector<string>::const_iterator it = locations.begin(); it != locations.end(); ++it)
			evidence.push_back(probLocationMap[*it]); // add the probabilities in order to the evidence vector
		return evidence;
	}
};

//int _tmain(int argc, _TCHAR* argv[])
int main(int argc, char* argv[])
{
	if (argc < 4) {
		cerr << "Usage: " << argv[0] << " two-consecutive-probabilistic-records-of-a-moving-object interval inferenceAlgorithm" << std::endl;
		return 1;
	}

	utilityclass ul;
	vector<probrecord> precs = ul.getProbRecords(argv[1]);
	probrecord &prec0 = precs.at(0);
	probrecord &prec1 = precs.at(1);
	int slices = ul.getNumberOfSlices(prec0, prec1, argv[2]);

	int theAlgorithm;
	switch (atoi(argv[3])) // available inference algorithms (belief updating algorithms) in GeNIe and SMILE
	{
	case 0: theAlgorithm = DSL_ALG_BN_LAURITZEN; break;
	case 1: theAlgorithm = DSL_ALG_BN_HENRION; break;
	case 2: theAlgorithm = DSL_ALG_BN_PEARL; break;
	case 3: theAlgorithm = DSL_ALG_BN_LSAMPLING; break;
	case 4: theAlgorithm = DSL_ALG_BN_SELFIMPORTANCE; break;
	case 5: theAlgorithm = DSL_ALG_BN_HEURISTICIMPORTANCE; break;
	case 6: theAlgorithm = DSL_ALG_BN_BACKSAMPLING; break;
	case 7: theAlgorithm = DSL_ALG_BN_AISSAMPLING; break;
	case 8: theAlgorithm = DSL_ALG_BN_EPISSAMPLING; break;
	case 9: theAlgorithm = DSL_ALG_BN_LBP; break;
	case 10: theAlgorithm = DSL_ALG_BN_LAURITZEN_OLD; break;
	case 11: theAlgorithm = DSL_ALG_BN_RELEVANCEDECOMP; break;
	case 12: theAlgorithm = DSL_ALG_BN_RELEVANCEDECOMP2; break;
	case 13: theAlgorithm = DSL_ALG_HBN_HEPIS; break;
	case 14: theAlgorithm = DSL_ALG_HBN_HLW; break;
	case 15: theAlgorithm = DSL_ALG_HBN_HLBP; break;
	case 16: theAlgorithm = DSL_ALG_HBN_HLOGICSAMPLING; break;
	default: theAlgorithm = DSL_ALG_BN_LAURITZEN; break;
	}

	dbn net("ExperimentDBN.xdsl", DSL_XDSL_FORMAT,  theAlgorithm);
	net.setNumberOfSlices(slices);

	vector<string> locationOutcomes = net.getLocationOutcomes();
	vector<double> evidence = ul.getEvidence(prec0, locationOutcomes);
	net.setTemporalVirtualEvidence(0, 0, evidence);
	evidence = ul.getEvidence(prec1, net.getLocationOutcomes());
	net.setTemporalVirtualEvidence(0, slices - 1, evidence);
	net.updateBeliefs();

	vector<double> probabilityDistribution;
	stringstream liness;
	for (int i = 0; i < slices; i++)
	{
		probabilityDistribution = net.getProbabilityDistribution(0, i);
		for (int j = 0; j < locationOutcomes.size(); j++)
		{
			liness << locationOutcomes.at(j) << ":" << probabilityDistribution.at(j) << ",";
		}
		string line1 = liness.str();
		string line2 = line1.substr(0, line1.length()-1);
		cout << line2 << endl;
		liness.str("");
	}

	return 0;
}