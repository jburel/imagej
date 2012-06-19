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

package imagej.ui.swing.plugins.debug;

import imagej.ImageJ;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.ImageJEvent;
import imagej.service.AbstractService;
import imagej.service.Service;

import java.util.ArrayList;
import java.util.Set;

/**
 * Service that keeps a history of ImageJ events.
 * 
 * @author Curtis Rueden
 */
@Service
public class EventHistory extends AbstractService {

	/** Event details that have been recorded. */
	private final ArrayList<EventDetails> history = new ArrayList<EventDetails>();
	
	private final ArrayList<EventHistoryListener> listeners =
		new ArrayList<EventHistoryListener>();

	private boolean active;

	// -- Constructors --

	public EventHistory() {
		// NB: Required by SezPoz.
		super(null);
		throw new UnsupportedOperationException();
	}

	public EventHistory(final ImageJ context, final EventService eventService) {
		super(context);
		subscribeToEvents(eventService);
	}

	// -- EventWatchService methods --

	public void setActive(final boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void clear() {
		history.clear();
	}
	
	/**
	 * Gets the recorded event history as an HTML string.
	 * 
	 * @param filtered Set of event types to filter out from the history.
	 * @param highlighted Set of event types to highlight in the history.
	 * @return An HTML string representing the recorded event history.
	 */
	public String toHTML(final Set<Class<? extends ImageJEvent>> filtered,
		final Set<Class<? extends ImageJEvent>> highlighted)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("<html><body style=\"font-family: monospaced;\">");
		for (final EventDetails details : history) {
			final Class<? extends ImageJEvent> eventClass = details.getEventClass();
			if (filtered != null && filtered.contains(eventClass)) {
				// skip filtered event type
				continue;
			}
			final boolean bold =
				highlighted != null && highlighted.contains(eventClass);
			sb.append(details.toHTML(bold));
			sb.append("<br>");
		}
		sb.append("</body></html>");
		return sb.toString();
	}

	public void addListener(final EventHistoryListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
		// someone is listening; start recording
		setActive(true);
	}

	public void removeListener(final EventHistoryListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
		if (listeners.isEmpty()) {
			// if no one is listening, stop recording
			setActive(false);
		}
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final ImageJEvent event) {
		if (!active) return; // only record events while active
		final EventDetails details = new EventDetails(event);
		history.add(details);
		notifyListeners(details);
	}

	// -- Helper methods --

	private void notifyListeners(final EventDetails details) {
		synchronized (listeners) {
			for (final EventHistoryListener l : listeners) {
				l.eventOccurred(details);
			}
		}
	}

}
