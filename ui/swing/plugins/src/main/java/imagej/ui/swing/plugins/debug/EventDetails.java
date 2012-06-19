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

import imagej.event.ImageJEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Caches details of a particular {@link ImageJEvent}, without saving the event
 * itself (since doing so could leave dangling references).
 * 
 * @author Curtis Rueden
 */
public class EventDetails {

	private final Date timestamp;
	private final Class<? extends ImageJEvent> eventClass;
	private final String eventString;

	public EventDetails(final ImageJEvent event) {
		timestamp = new Date();
		eventClass = event.getClass();
		eventString = event.toString();
	}

	// -- EventDetails methods --

	public Class<? extends ImageJEvent> getEventClass() {
		return eventClass;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getEventString() {
		return eventString;
	}

	public String toHTML(final boolean bold) {
		final StringBuilder sb = new StringBuilder();
		if (bold) sb.append("<strong>");

		// append timestamp
		sb.append("<font color=\"gray\">[");
		sb.append(timestampAsString());
		sb.append("] </font>");

		// append event class name
		sb.append("<font color=\"green\">");
		sb.append(eventClass.getSimpleName());
		sb.append("</font>");

		// append event string
		sb.append("<font color=\"black\">");
		sb.append(eventString);
		sb.append("</font>");

		if (bold) sb.append("</strong>");
		return sb.toString();
	}

	// -- Helper methods --

	private String timestampAsString() {
		final SimpleDateFormat formatter =
			new SimpleDateFormat("hh:mm:ss.SS", Locale.getDefault());
		final String dateStr = formatter.format(timestamp);
		return dateStr;
	}

}
