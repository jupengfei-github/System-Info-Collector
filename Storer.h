#ifndef _STORER_H_
#define _STORER_H_

using namespace std;
using namespace android;

namespace sim {

class Storer {
	enum BufferType {
		BUFFER_NONE,
		BUFFER_CYCLE,
		BUFFER_QUEUE,
	};

public:
	virtual int init()   = 0;
	virtual int finish() = 0;
	virtual int pause()  = 0;

	/* buffer */
	void setBufferType (enum BufferType type);
	void setBufferCycleSize (size_t size);
	virtual int flushBuffer () = 0;

	virtual int writeData (uint8_t *data, size_t size);
};

}; /* namespace */

#endif