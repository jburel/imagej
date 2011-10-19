//
// DirectoryChooserTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package ij.io;

import static org.junit.Assert.assertEquals;
import ij.IJInfo;

import org.junit.Test;

/**
 * Unit tests for {@link DirectoryChooser}.
 *
 * @author Barry DeZonia
 */
public class DirectoryChooserTest {

	@Test
	public void testDirectoryChooser() {
		if (IJInfo.RUN_GUI_TESTS) {
			DirectoryChooser chooser = new DirectoryChooser("Choose any path");
			assertEquals(true,true);
		}
	}

	// this test is not done in a OS agnostic fashion!!! must fix
	// do these tests need to go away? Or do we need to make mock calls to choose a directory?
	@Test
	public void testGetDirectory() {
		if (IJInfo.RUN_GUI_TESTS) {
			DirectoryChooser chooser = new DirectoryChooser("Choose path called /System/");
			assertEquals("/System/", chooser.getDirectory());
		}
	}

	@Test
	public void testSetDefaultDirectory() {
		if (IJInfo.RUN_GUI_TESTS) {
			DirectoryChooser.setDefaultDirectory("/Library/");
			DirectoryChooser chooser = new DirectoryChooser("See that path == /Library/");
			assertEquals(true,true);
		}
	}

}