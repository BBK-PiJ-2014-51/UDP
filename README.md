UDP (CW5)
Audio Streaming Client & Server
By Caleb Clayton

Basic workflow:
		An audio server is launched, opens tcp and begins accepting connections. The first client to connect becomes the audio
	provider. The audio provider submits audio to server in chunks which are stored in a buffer. All subsequent client
	connections are audio listeners - they receive chunks from the server's buffer that is filled by the provider.
	
		Config options are available for items such as max connections, default ports, buffer sizes, etc. Unfortunately I didn't
	have time to make a proper config file so they are just listed as constants.
	
		Clients began streaming the audio in realtime as they connect. The server waits until all connected clients have received 
	the last packet issued before the next packet becomes available. The audio provider must wait to write if it gets too far ahead 
	of the listeners in order to prevent the provder wrapping around the buffer and writing over bits before they are read.
		
		If the provder gets too far behind the listeners he assumed is disconencted or too slow and gets booted. If the provider is 
	disconnected the server is reset and all clients are messaged to reconnect. The first to reconnect will be new provider.

A few notes about the project:
	I. Audio must be a 16 bit, stereo wav sampled at 44.1 khz
	
	II. path to audio must be specifed correctly at line 182 of client implementation
	
	III. Unit testing proved difficult. While there is some basic test coverage, it may be more useful to just launch client / server
	instances. Audio can be heard and there is some basic indication of where the buffers are reading and writing from provided by
	standard out.