System Management - CPU Sample Application
=========================================

This application demonstrates the usage of the System Management - CPU and
memory APIs. It displays some panels to configure the CPU cores and settings and
monitoring some of the CPU and memory parameters.

Demo requirements
-----------------

To run this example you need:

* One compatible device to host the application.
* A USB connection between the device and the host PC in order to transfer and
  launch the application.

Demo setup
----------

Make sure the hardware is set up correctly:

1. The device is powered on.
2. The device is connected directly to the PC by the micro USB cable.

Demo run
--------

The example is already configured, so all you need to do now is to build and
launch the project.

While it is running, the application displays a set of panels. Each panel
displays specific functionality provided by the API:

  * CPU cores: Allows you to enable and disable the different CPU cores.
  * CPU configuration: Allows you to configure the CPU frequencies and
                       governor. Depending on the selected governor you can
                       configure advanced governor settings.
  * CPU usage: Displays the overall usage of the CPU in % as well as the
               usage of each core.
  * PI calculation test: Executes an arithmetic test calculating the
                         specified digits of the number Pi.
  * CPU and memory status: Displays instant values of some CPU and memory
                           parameters such as temperature, overall usage,
                           current frequency, and free vs total memory.

Compatible with
---------------

* ConnectCore 6 SBC
* ConnectCore 6 SBC v3

License
-------

Copyright (c) 2014-2016, Digi International Inc. <support@digi.com>

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.