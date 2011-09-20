//
// AbstractDataView.java
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

package imagej.data.display;

import imagej.data.Data;
import imagej.data.Extents;
import imagej.data.Position;
import imagej.data.display.event.DataViewDeselectedEvent;
import imagej.data.display.event.DataViewSelectedEvent;
import imagej.data.display.event.DataViewSelectionEvent;
import imagej.data.event.DataRestructuredEvent;
import imagej.data.event.DataUpdatedEvent;
import imagej.event.EventSubscriber;
import imagej.event.Events;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract supeclass for {@link DataView}s.
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractDataView implements DataView {

	private final ImageDisplay display;
	private final Data dataObject;

	/** List of event subscribers, to avoid garbage collection. */
	private final List<EventSubscriber<?>> subscribers =
		new ArrayList<EventSubscriber<?>>();

	private long[] planeDims;
	private long[] position;
	private Position planePosition;

	/** Indicates the view is no longer in use. */
	private boolean disposed;
	
	/**
	 * True if view is selected, false if not.
	 */
	private boolean selected;

	public AbstractDataView(final ImageDisplay display, final Data dataObject) {
		this.display = display;
		this.dataObject = dataObject;
		dataObject.incrementReferences();
		subscribeToEvents();
	}

	// -- DataView methods --

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	@Override
	public Data getData() {
		return dataObject;
	}

	@Override
	public Position getPlanePosition() {
		return planePosition;
	}

	@Override
	public long getPlaneIndex() {
		return planePosition.getIndex();
	}

	@Override
	public long getPosition(final int dim) {
		return position[dim];
	}
	
	@Override
	public void setPosition(final long value, final int dim) {
		position[dim] = value;
		if (dim >= 2)
			planePosition.setPosition(value, dim-2);
	}

	@Override
	public void dispose() {
		if (disposed) return;
		disposed = true;
		dataObject.decrementReferences();
	}

	// -- Helper methods --

	@Override
	public void setSelected(boolean isSelected) {
		if (selected != isSelected) {
			selected = isSelected;
			DataViewSelectionEvent event = isSelected? new DataViewSelectedEvent(this): new DataViewDeselectedEvent(this);
			Events.publish(event);
		}
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	/** Updates the display when the linked object changes. */
	private void subscribeToEvents() {
		final EventSubscriber<DataUpdatedEvent> updateSubscriber =
			new EventSubscriber<DataUpdatedEvent>()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void onEvent(final DataUpdatedEvent event) {
				if (event.getObject() != dataObject) return;
				update();
				display.update();
			}
		};
		Events.subscribe(DataUpdatedEvent.class, updateSubscriber);
		subscribers.add(updateSubscriber);

		// TODO - perhaps it would be better for the display to listen for
		// ObjectRestructuredEvents, compare the data object to all of its views,
		// and call rebuild() on itself (only once). This would avoid a potential
		// issue where multiple views linked to the same data object will currently
		// result in multiple rebuilds.
		final EventSubscriber<DataRestructuredEvent> restructureSubscriber =
			new EventSubscriber<DataRestructuredEvent>()
		{
			@SuppressWarnings("synthetic-access")
			@Override
			public void onEvent(final DataRestructuredEvent event) {
				if (event.getObject() != dataObject) return;
				rebuild();
				display.update();
			}
		};
		Events.subscribe(DataRestructuredEvent.class, restructureSubscriber);
		subscribers.add(restructureSubscriber);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	public void setDimensions(long[] dims) {
		position = new long[dims.length];
		planeDims = new long[dims.length-2];
		for (int i = 0; i < planeDims.length; i++)
			planeDims[i] = dims[i+2];
		Extents extents = new Extents(planeDims);
		planePosition = extents.createPosition();
		planePosition.first();
	}
}