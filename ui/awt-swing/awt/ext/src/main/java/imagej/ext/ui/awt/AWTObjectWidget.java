//
// AWTObjectWidget.java
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

package imagej.ext.ui.awt;

import imagej.ext.module.ui.ObjectWidget;
import imagej.ext.module.ui.WidgetModel;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * AWT implementation of object selector widget.
 * 
 * @author Curtis Rueden
 */
public class AWTObjectWidget extends AWTInputWidget
	implements ItemListener, ObjectWidget
{

	private final Choice choice;
	private final Object[] items;

	public AWTObjectWidget(final WidgetModel model, final Object[] items) {
		super(model);
		this.items = items;

		choice = new Choice();
		for (final Object item : items) choice.add(item.toString());
		add(choice, BorderLayout.CENTER);
		choice.addItemListener(this);

		refreshWidget();
	}

	// -- InputWidget methods --

	@Override
	public Object getValue() {
		return items[choice.getSelectedIndex()];
	}

	@Override
	public void refreshWidget() {
		choice.select(getValidValue());
	}

	// -- ItemListener methods --

	@Override
	public void itemStateChanged(ItemEvent e) {
		updateModel();
	}

	// -- Helper methods --

	private String getValidValue() {
		final int itemCount = choice.getItemCount();
		if (itemCount == 0) return null; // no valid values exist

		final String value = getModel().getValue().toString();
		for (int i = 0; i < itemCount; i++) {
			final String item = choice.getItem(i);
			if (value == item) return value;
		}

		// value was invalid; reset to first choice on the list
		final String validValue = choice.getItem(0);
		// CTR FIXME should not update model in getter!
		getModel().setValue(validValue);
		return validValue;
	}

}