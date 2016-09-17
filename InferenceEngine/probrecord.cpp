#include "StdAfx.h"
#include "probrecord.h"

using namespace std;
/*
 * Author: Sari Haj Hussein
 */
/** public methods **/
void probrecord::info()
{
	cout << "General Prob Record Information" << endl;
	cout << "-------------------------------" << endl;
	cout << "License Plate: " << this->licensePlate << endl;
	cout << "Prob Location: " << this->probLocation << endl;
	cout << "Start Time: " << this->sTime << endl;
	cout << "End Time: " << this->eTime << endl;
	cout << "Start Time Unix: " << this->sTimeUnix << endl;
	cout << "End Time Unix: " << this->eTimeUnix << endl;
}

string probrecord::getLicensePlate()
{
	return this->licensePlate;
}

string probrecord::getProbLocation()
{
	return this->probLocation;
}

string probrecord::getsTime()
{
	return this->sTime;
}

string probrecord::geteTime()
{
	return this->eTime;
}

__int64 probrecord::getsTimeUnix()
{
	return this->sTimeUnix;
}

__int64 probrecord::geteTimeUnix()
{
	return this->eTimeUnix;
}

/** constructor **/
probrecord::probrecord(string licensePlate, string probLocation, string sTime, string eTime, __int64 sTimeUnix, __int64 eTimeUnix)
{
	this->licensePlate = licensePlate;
	this->probLocation = probLocation;
	this->sTime = sTime;
	this->eTime = eTime;
	this->sTimeUnix = sTimeUnix;
	this->eTimeUnix = eTimeUnix;
}

/** destructor **/
probrecord::~probrecord()
{
}