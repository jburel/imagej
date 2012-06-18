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

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.HashSet;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * Swing-specific window for event watcher plugin.
 * 
 * @author Curtis Rueden
 */
public class WatchEventsFrame extends JFrame implements TreeSelectionListener {

	private static final int STARTING_TREE_WIDTH = 500;
	private static final int STARTING_TEXT_WIDTH = 400;

	private final EventHistory eventHistory;

	private final HashSet<Class<? extends ImageJEvent>> filtered =
		new HashSet<Class<? extends ImageJEvent>>();
	private final HashSet<Class<? extends ImageJEvent>> selected =
		new HashSet<Class<? extends ImageJEvent>>();

	private final DefaultTreeModel treeModel;
	private final DefaultMutableTreeNode root;
	private final JTree tree;

	private final JEditorPane textArea;

	// TODO: add text filter based on selected tree node(s)

	// -- Constructor --

	public WatchEventsFrame(final EventHistory eventHistory) {
		super("Event Watcher");
		this.eventHistory = eventHistory;

		root = create(ImageJEvent.class);
		treeModel = new DefaultTreeModel(root);

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		setContentPane(splitPane);

		tree = createTree();
		splitPane.add(new JScrollPane(tree));
		textArea = createTextArea();
		splitPane.add(new JScrollPane(textArea));

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		pack();
	}

	// -- WatchEventsFrame methods --

	public void append(final String text) {
		// CTR FIXME
		setText(eventHistory.toHTML(filtered, selected));
//		textArea.append(text);
		scrollToBottom();
	}

	public void setText(final String text) {
		textArea.setText(text);
		scrollToBottom();
	}

	public void clear() {
		textArea.setText("");
	}

	public void append(final EventDetails details) {
		final DefaultMutableTreeNode node = findOrCreate(details.getEventClass());
		final EventTypeInfo info = getInfo(node);
		if (!info.enabled) return; // skip disabled event types
	}

	// -- TreeSelectionListener methods --

	@Override
	public void valueChanged(final TreeSelectionEvent e) {
		refreshSelected();
	}

	// -- Helper methods --

	private void refreshFiltered() {
		filtered.clear();
		refreshFiltered(root);
	}

	private void refreshFiltered(final DefaultMutableTreeNode node) {
		final EventTypeInfo info = getInfo(node);
		final Class<? extends ImageJEvent> eventClass = info.eventClass;
		if (!info.enabled) filtered.add(eventClass);

		@SuppressWarnings("unchecked")
		final Enumeration<DefaultMutableTreeNode> en = node.children();
		while (en.hasMoreElements()) {
			final DefaultMutableTreeNode child = en.nextElement();
			refreshFiltered(child);
		}
	}

	private void refreshSelected() {
		selected.clear();
		final TreePath[] paths = tree.getSelectionPaths();
		for (final TreePath path : paths) {
			for (final Object o : path.getPath()) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
				final EventTypeInfo info = getInfo(node);
				selected.add(info.eventClass);
			}
		}
	}

	private JTree createTree() {
		final JTree t = new JTree(treeModel);
		t.setPreferredSize(new Dimension(STARTING_TREE_WIDTH, 0));
		t.setShowsRootHandles(true);
		t.addTreeSelectionListener(this);
		return t;
	}

	private JEditorPane createTextArea() {
		final JEditorPane text = new JEditorPane();
		text.setEditable(false);
		text.setPreferredSize(new Dimension(STARTING_TEXT_WIDTH, 0));
		return text;
	}

	/** Gets a tree node for the given type of event, creating it if necessary. */
	private DefaultMutableTreeNode findOrCreate(
		final Class<? extends ImageJEvent> eventClass)
	{
		if (eventClass == null) return null;
		if (eventClass == ImageJEvent.class) return root;
		@SuppressWarnings("unchecked")
		final Class<? extends ImageJEvent> superclass =
			(Class<? extends ImageJEvent>) eventClass.getSuperclass();
		final DefaultMutableTreeNode parentNode = findOrCreate(superclass);

		@SuppressWarnings("unchecked")
		final Enumeration<DefaultMutableTreeNode> en = parentNode.children();
		while (en.hasMoreElements()) {
			final DefaultMutableTreeNode child = en.nextElement();
			final EventTypeInfo info = getInfo(child);
			if (info.eventClass == eventClass) {
				// found existing event type in the tree
				return child;
			}
		}

		// event type is new; add it to the tree
		final DefaultMutableTreeNode node = create(eventClass);
		parentNode.add(node);

		// refresh the tree
		treeModel.reload();
		tree.expandPath(new TreePath(parentNode.getPath()));
		tree.scrollPathToVisible(new TreePath(node.getPath()));

		return node;
	}

	/** Creates a tree node for the given type of event. */
	private DefaultMutableTreeNode create(
		final Class<? extends ImageJEvent> eventClass)
	{
		return new DefaultMutableTreeNode(new EventTypeInfo(eventClass));
	}

	/** Gets the event info associated with the given tree node. */
	private EventTypeInfo getInfo(final DefaultMutableTreeNode node) {
		return (EventTypeInfo) node.getUserObject();
	}

	/** Makes sure the last line of text is always visible. */
	private void scrollToBottom() {
		textArea.setCaretPosition(textArea.getDocument().getLength());
	}

	// -- Helper classes --

	/**
	 * A simple data structure to store a type of event and toggle state with a
	 * particular tree node.
	 */
	private class EventTypeInfo {

		private final Class<? extends ImageJEvent> eventClass;
		private final boolean enabled;

		public EventTypeInfo(final Class<? extends ImageJEvent> eventClass) {
			this.eventClass = eventClass;
			enabled = true;
		}

		@Override
		public String toString() {
			return eventClass.getName();
		}
	}

}
