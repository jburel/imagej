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

package imagej.ui;

import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.StatusService;
import imagej.ext.display.Display;
import imagej.ext.menu.MenuService;
import imagej.ext.plugin.PluginService;
import imagej.ext.tool.ToolService;
import imagej.options.OptionsService;
import imagej.platform.AppService;
import imagej.platform.PlatformService;
import imagej.platform.event.AppQuitEvent;
import imagej.service.IService;
import imagej.thread.ThreadService;

import java.util.List;

/**
 * Interface for service that handles ImageJ user interfaces.
 * 
 * @author Curtis Rueden
 */
public interface UIService extends IService {

	EventService getEventService();

	StatusService getStatusService();

	ThreadService getThreadService();

	PlatformService getPlatformService();

	PluginService getPluginService();

	MenuService getMenuService();

	ToolService getToolService();

	OptionsService getOptionsService();

	AppService getAppService();

	void createUI();

	/** Processes the given command line arguments. */
	void processArgs(String[] args);

	/** Gets the active user interface. */
	UserInterface getUI();

	/** Gets the user interfaces available on the classpath. */
	List<UserInterface> getAvailableUIs();

	/** Creates a new output window. */
	OutputWindow createOutputWindow(String title);

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message);

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title);

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title,
		DialogPrompt.MessageType messageType);

	/**
	 * Displays a dialog prompt.
	 * 
	 * @param message The message in the dialog itself.
	 * @param title The title of the dialog.
	 * @param messageType The type of message. This typically is rendered as an
	 *          icon next to the message. For example,
	 *          {@link DialogPrompt.MessageType#WARNING_MESSAGE} typically appears
	 *          as an exclamation point.
	 * @param optionType The choices available when dismissing the dialog. These
	 *          choices are typically rendered as buttons for the user to click.
	 * @return The choice selected by the user when dismissing the dialog.
	 */
	DialogPrompt.Result showDialog(String message, String title,
		DialogPrompt.MessageType messageType, DialogPrompt.OptionType optionType);

	/**
	 * Displays a popup context menu for the given display at the specified
	 * position.
	 */
	void showContextMenu(String menuRoot, Display<?> display, int x, int y);

	@EventHandler
	void onEvent(AppQuitEvent event);

}
