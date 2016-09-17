#pragma once

#include <iostream>
#include <string>

using namespace std;
/*
 * Author: Sari Haj Hussein
 */
class probrecord
{
public:
	/** public methods **/
	void info();
	string getLicensePlate();
	string getProbLocation();
	string getsTime();
	string geteTime();
	__int64 getsTimeUnix();
	__int64 geteTimeUnix();

	/** constructor **/
	probrecord(string licensePlate, string probLocation, string sTime, string eTime, __int64 sTimeUnix, __int64 eTimeUnix);
	/** destructor **/
	virtual ~probrecord();

private:
	/** instance variables **/
	string licensePlate;
	string probLocation;
	string sTime;
	string eTime;
	__int64 sTimeUnix;
	__int64 eTimeUnix;
};