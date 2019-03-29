#ifndef _COLLECTOR_H_
#define _COLLECTOR_H_

#include "Storer.h"

using namespace std;
using namespace android;

namespace sic {

class Collecotr {
	enum Mode {
		MODE_MULTIPLEXING,
		MODE_SIGNLE,
	};

private:
	String16 mName;
	sp<Storer> mStorer;
	Modem mMode;

public:
	Collecotr (String16 name, sp<Storer> storer) {
		mName   = name;
		mStorer = storer;
	}

	Collecotr (String16 name, sp<Storer> storer, enum Mode mode):Collecotr(name, storer):mMode(mode) {}

	virtual int start() = 0;
	virtual int stop()  = 0;
	virtual int pause() = 0;

	void int changeStorer (sp<Storer> storer);
};

}; /* namespace */

#endif