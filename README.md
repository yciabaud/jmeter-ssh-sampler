# jmeter-ssh-sampler [![travis][travis-image]][travis-url]

[travis-image]: https://travis-ci.org/yciabaud/jmeter-ssh-sampler.svg?branch=master
[travis-url]: https://travis-ci.org/yciabaud/jmeter-ssh-sampler

Overview
------------

SSH Sampler for Jakarta JMeter that executes commands (eg, iostat) over an SSH session, and returns the output.
The output may then be parsed or logged by a listener for use elsewhere in the testing process.
This repository is a fork of http://code.google.com/p/jmeter-ssh-sampler/ to manage pull reqests.

A second component deals with SFTP to allow you to download files over SSH to assert on their content.

Installation
------------

Installation is fairly straightforward, and only involves adding the plugin and JSch to the right directory:

1. Build with maven
2. Place the jmeter-ssh-sampler jar file into JMeter's lib/ext directory
3. Place JSch into JMeter's lib directory
4. Run JMeter, and find "SSH Sampler" in the Samplers category

Usage
------------

Using the plugin is simple (assuming familiarity with SSH and JMeter):

### SSH Command

1. Create a new Test Plan
2. Add a Thread Group
3. Add a Sampler > SSH Command
4. Specify the host to connect to, port, username and password (unencrypted) or a key file, and a command to execute (such as date)
5. Add a Listener > View Results Tree
6. Run the test


### SSH SFTP

1. Create a new Test Plan
2. Add a Thread Group
3. Add a Sampler > SSH SFTP
4. Specify the host to connect to, port, username and password (unencrypted) or a key file, and a SFTP command to execute (such as get)
5. Specify a source file to download, an output file or to print the content in the test
5. Add a Listener > View Results Tree
6. Run the test

Dependencies
------------

Maven retrieves the following dependencies:

* SSH functionality is provided by the JSch library
* JMeter 2.3+ is capable of running this plugin



Contributing
------------

1. Fork it.
2. Create a branch (`git checkout -b my_plugin`)
3. Commit your changes (`git commit -am "Added feature"`)
4. Push to the branch (`git push origin my_plugin`)
5. Create an [Issue][1] with a link to your branch
6. Enjoy a refreshing Diet Coke and wait
