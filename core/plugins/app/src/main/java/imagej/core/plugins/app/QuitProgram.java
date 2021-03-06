/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.core.plugins.app;

import imagej.data.display.WindowMenuService;
import imagej.ext.menu.MenuConstants;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Menu;
import imagej.ext.plugin.Parameter;
import imagej.ext.plugin.Plugin;
import imagej.ui.DialogPrompt;
import imagej.ui.UIService;

/**
 * Quits ImageJ.
 * 
 * @author Grant Harris
 * @author Barry DeZonia
 * @author Curtis Rueden
 */
@Plugin(iconPath = "/icons/plugins/door_in.png", menu = {
	@Menu(label = MenuConstants.FILE_LABEL, weight = MenuConstants.FILE_WEIGHT,
		mnemonic = MenuConstants.FILE_MNEMONIC),
	@Menu(label = "Quit", weight = Double.MAX_VALUE, mnemonic = 'q',
		accelerator = "control Q") }, headless = true)
public class QuitProgram implements ImageJPlugin {

	public static final String MESSAGE = "Quit ImageJ?";

	@Parameter
	private WindowMenuService windowMenuService;

	@Parameter
	private UIService uiService;

	@Override
	public void run() {
		if (windowMenuService != null &&
			windowMenuService.getOpenWindows().size() > 0)
		{
			if (!promptForQuit()) {
				return;
			}

			// TODO - save existing data
			// TODO - close windows
		}
		// TODO - call ImageJ.getContext().shutdown() or some such, rather than
		// using System.exit(0), which kills the entire JVM.
		System.exit(0);
	}

	private boolean promptForQuit() {
		final DialogPrompt.Result result =
			uiService.showDialog(MESSAGE, "Quit",
				DialogPrompt.MessageType.QUESTION_MESSAGE,
				DialogPrompt.OptionType.YES_NO_OPTION);
		return result == DialogPrompt.Result.YES_OPTION;
	}

}
