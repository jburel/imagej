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

package imagej.ui.swing;

import imagej.ImageJ;
import imagej.core.plugins.overlay.SelectedManagerOverlayProperties;
import imagej.data.ChannelCollection;
import imagej.data.Dataset;
import imagej.data.DatasetService;
import imagej.data.display.DataView;
import imagej.data.display.DatasetView;
import imagej.data.display.ImageDisplay;
import imagej.data.display.ImageDisplayService;
import imagej.data.display.OverlayInfo;
import imagej.data.display.OverlayInfoList;
import imagej.data.display.OverlayService;
import imagej.data.display.OverlayView;
import imagej.data.display.event.DataViewSelectionEvent;
import imagej.data.event.OverlayCreatedEvent;
import imagej.data.event.OverlayDeletedEvent;
import imagej.data.event.OverlayRestructuredEvent;
import imagej.data.event.OverlayUpdatedEvent;
import imagej.data.overlay.Overlay;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.EventSubscriber;
import imagej.ext.display.DisplayService;
import imagej.ext.plugin.PluginService;
import imagej.options.OptionsService;
import imagej.options.plugins.OptionsChannels;
import imagej.platform.PlatformService;
import imagej.util.Log;
import imagej.util.Prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.imglib2.RandomAccess;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.Axes;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

// TODO
//
// - implement methods that actually do stuff
// - since it knows its a Swing UI it uses Swing UI features. Ideally we should
//   make OverlayManager a Display<Overlay> and work as much as possible in
//   an agnostic fashion. Thus no swing style listeners but instead IJ2
//   listeners. And rather than swing input dialogs we should make IJ2 input
//   dialogs.

/**
 * Overlay Manager Swing UI
 * 
 * @author Barry DeZonia
 * @author Adam Fraser
 */
public class SwingOverlayManager
	extends JFrame
	implements ActionListener, ItemListener
{

	// -- constants --
	
	//private static final long serialVersionUID = -6498169032123522303L;

	// no longer supported
	//private static final String ACTION_ADD = "add";
	private static final String ACTION_ADD_PARTICLES = "add particles";
	private static final String ACTION_AND = "and";
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_DESELECT = "deselect";
	private static final String ACTION_DRAW = "draw";
	private static final String ACTION_FILL = "fill";
	private static final String ACTION_FLATTEN = "flatten";
	private static final String ACTION_HELP = "help";
	private static final String ACTION_MEASURE = "measure";
	private static final String ACTION_MULTI_MEASURE = "multi measure";
	private static final String ACTION_MULTI_PLOT = "multi plot";
	private static final String ACTION_OPEN = "open";
	private static final String ACTION_OPTIONS = "options";
	private static final String ACTION_OR = "or";
	private static final String ACTION_PROPERTIES = "properties";
	private static final String ACTION_REMOVE_SLICE_INFO = "remove slice info";
	private static final String ACTION_RENAME = "rename";
	private static final String ACTION_SAVE = "save";
	private static final String ACTION_SORT = "sort";
	private static final String ACTION_SPECIFY = "specify";
	private static final String ACTION_SPLIT = "split";
	// no longer supported
	//private static final String ACTION_UPDATE = "update";
	private static final String ACTION_XOR = "xor";
	
	private static final String LAST_X = "lastXLocation";
	private static final String LAST_Y = "lastYLocation";

	// -- instance variables --
	
	/** Maintains the list of event subscribers, to avoid garbage collection. */
	@SuppressWarnings("unused")
	private final List<EventSubscriber<?>> subscribers;
	private final ImageJ context;
	private final JList jlist;
	private boolean selecting = false; // flag to prevent event feedback loops
	private JPopupMenu popupMenu = null;
	private final JCheckBox showAllCheckBox;
	private final JCheckBox editModeCheckBox;
	private boolean shiftDown = false;
	private boolean altDown = false;
	private final OverlayService ovrSrv;
	
	// -- constructor --
	
	/**
	 * Creates a JList to list the overlays. 
	 */
	public SwingOverlayManager(final ImageJ context) {
		this.context = context;
		this.ovrSrv = context.getService(OverlayService.class);
		jlist = new JList(new OverlayListModel(ovrSrv.getOverlayInfo()));
		//jlist.setCellRenderer(new OverlayRenderer());

		final JScrollPane listScroller = new JScrollPane(jlist);
		listScroller.setPreferredSize(new Dimension(250, 80));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		final JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.add(listScroller);
		listPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new GridLayout(9,1,5,0));
		// NO LONGER SUPPORTING
		//buttonPane.add(getAddButton());
		//buttonPane.add(getUpdateButton());
		buttonPane.add(getDeleteButton());
		buttonPane.add(getRenameButton());
		buttonPane.add(getMeasureButton());
		buttonPane.add(getDeselectButton());
		buttonPane.add(getPropertiesButton());
		buttonPane.add(getFlattenButton());
		buttonPane.add(getFillButton());
		buttonPane.add(getDrawButton());
		buttonPane.add(getMoreButton());

		final JPanel boolPane = new JPanel();
		boolPane.setLayout(new BoxLayout(boolPane, BoxLayout.Y_AXIS));
		showAllCheckBox = new JCheckBox("Show All",false);
		editModeCheckBox = new JCheckBox("Edit Mode",false);
		boolPane.add(showAllCheckBox);
		boolPane.add(editModeCheckBox);
		showAllCheckBox.addItemListener(this);
		editModeCheckBox.addItemListener(this);
		
		final JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BorderLayout());
		controlPanel.add(buttonPane,BorderLayout.CENTER);
		controlPanel.add(boolPane,BorderLayout.SOUTH);

		final Container cp = this.getContentPane();
		cp.add(listPanel, BorderLayout.CENTER);
		cp.add(controlPanel, BorderLayout.EAST);

		setTitle("Overlay Manager");
		setupListSelectionListener();
		setupCloseListener();
		setupKeyListener();
		restoreLocation();
		
		pack();
		
		final EventService eventService = context.getService(EventService.class);
		subscribers = eventService.subscribe(this);

		/* NOTE BDZ removed 6-11-12. There should be a default menu bar attached
		 * to this frame now.
		 *
		// FIXME - temp hack - made this class (which is not a display) make sure
		// menu bar available when it is running. Ugly cast in place to create the
		// menu bar. A better approach would be to make a new event tied to a menu
		// bar listener of some sort. This code could emit that "need a menu bar"
		// event here. Filing as ticket.
		
		final UIService uiService = context.getService(UIService.class);
		((AbstractSwingUI)uiService.getUI()).createMenuBar(this, false);
		*/
		
		populateOverlayList();
	}

	// -- public interface --
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		final String command = e.getActionCommand();
		if (command == null) return;
		/* no longer supported
		if (command.equals(ACTION_ADD))
			add();
		*/
		if (command.equals(ACTION_ADD_PARTICLES))
			addParticles();
		else if (command.equals(ACTION_AND))
			and();
		else if (command.equals(ACTION_DELETE))
			delete();
		else if (command.equals(ACTION_DESELECT))
			deselect();
		else if (command.equals(ACTION_DRAW))
			draw();
		else if (command.equals(ACTION_FILL))
			fill();
		else if (command.equals(ACTION_FLATTEN))
			flatten();
		else if (command.equals(ACTION_HELP))
			help();
		else if (command.equals(ACTION_MEASURE))
			measure();
		else if (command.equals(ACTION_MULTI_MEASURE))
			multiMeasure();
		else if (command.equals(ACTION_MULTI_PLOT))
			multiPlot();
		else if (command.equals(ACTION_OPEN))
			open();
		else if (command.equals(ACTION_OPTIONS))
			options();
		else if (command.equals(ACTION_OR))
			or();
		else if (command.equals(ACTION_PROPERTIES))
			properties();
		else if (command.equals(ACTION_REMOVE_SLICE_INFO))
			removeSliceInfo();
		else if (command.equals(ACTION_RENAME))
			rename();
		else if (command.equals(ACTION_SAVE))
			save();
		else if (command.equals(ACTION_SORT))
			sort();
		else if (command.equals(ACTION_SPECIFY))
			specify();
		else if (command.equals(ACTION_SPLIT))
			split();
		/*
		else if (command.equals(ACTION_UPDATE))
			update();
		*/
		else if (command.equals(ACTION_XOR))
			xor();
	}

	// -- private helpers for overlay list maintenance --

	
	private class OverlayListModel extends AbstractListModel {

		//private static final long serialVersionUID = 7941252533859436640L;

		private OverlayInfoList overlayInfoList;
		
		public OverlayListModel(OverlayInfoList list) {
			overlayInfoList = list;
		}
		
		@Override
		public Object getElementAt(final int index) {
			return overlayInfoList.getOverlayInfo(index);
		}

		@Override
		public int getSize() {
			return overlayInfoList.getOverlayInfoCount();
		}

	}

	/*
	*/
	private void populateOverlayList() {
		// Populate the list with all overlays
		for (final Overlay overlay : ovrSrv.getOverlays()) {
			boolean found = false;
			int totOverlays = ovrSrv.getOverlayInfo().getOverlayInfoCount();
			for (int i = 0; i < totOverlays; i++) {
				OverlayInfo info = ovrSrv.getOverlayInfo().getOverlayInfo(i);
				if (overlay == info.getOverlay()) {
					found = true;
					break;
				}
			}
			if (!found) {
				OverlayInfo info = new OverlayInfo(overlay);
				ovrSrv.getOverlayInfo().addOverlayInfo(info);
			}
		}
		jlist.updateUI();
	}
	
	/*
	private class OverlayRenderer extends DefaultListCellRenderer {

		//private static final long serialVersionUID = 2468086636364454253L;
		private final Hashtable<Overlay, ImageIcon> iconTable =
			new Hashtable<Overlay, ImageIcon>();

		@Override
		public Component getListCellRendererComponent(final JList list,
			final Object value, final int index, final boolean isSelected,
			final boolean hasFocus)
		{
			final JLabel label =
				(JLabel) super.getListCellRendererComponent(list, value, index,
					isSelected, hasFocus);
			if (value instanceof Overlay) {
				final Overlay overlay = (Overlay) value;
				// TODO: create overlay thumbnail from overlay
				final ImageIcon icon = iconTable.get(overlay);
//				if (icon == null) {
//					icon = new ImageIcon(...);
//					iconTable.put(overlay, ImageIcon);
//				}
				label.setIcon(icon);
			}
			else {
				// Clear old icon; needed in 1st release of JDK 1.2
				label.setIcon(null);
			}
			return label;
		}

	}
	*/

	// -- event handlers --

	@EventHandler
	protected void onEvent(final OverlayCreatedEvent event) {
		//System.out.println("\tCREATED: " + event.toString());
		ovrSrv.getOverlayInfo().addOverlay(event.getObject());
		jlist.updateUI();
	}

	@EventHandler
	protected void onEvent(final OverlayDeletedEvent event) {
		//System.out.println("\tDELETED: " + event.toString());
		Overlay overlay = event.getObject();
		ovrSrv.getOverlayInfo().deleteOverlay(overlay);
		int[] newSelectedIndices = ovrSrv.getOverlayInfo().selectedIndices();
		jlist.setSelectedIndices(newSelectedIndices);
		jlist.updateUI();
	}
	
	/*
	// Update when a display is activated.
	@EventHandler
	protected void onEvent(
		@SuppressWarnings("unused") final DisplayActivatedEvent event)
	{
		jlist.updateUI();
	}
	*/

	@EventHandler
	protected void onEvent(final DataViewSelectionEvent event) {
		if (selecting) return;
		selecting = true;
		// Select or deselect the corresponding overlay in the list
		final Overlay overlay = (Overlay) event.getView().getData();
		final int overlayIndex = ovrSrv.getOverlayInfo().findIndex(overlay);
		final OverlayInfo overlayInfo = ovrSrv.getOverlayInfo().getOverlayInfo(overlayIndex);
		overlayInfo.setSelected(event.isSelected());
		/* old way
		if (event.isSelected()) {
			final int[] current_sel = jlist.getSelectedIndices();
			jlist.setSelectedValue(overlayInfo, true);
			final int[] new_sel = jlist.getSelectedIndices();
			final int[] sel =
				Arrays.copyOf(current_sel, current_sel.length + new_sel.length);
			System.arraycopy(new_sel, 0, sel, current_sel.length, new_sel.length);
			jlist.setSelectedIndices(sel);
		}
		else {
			for (final int i : jlist.getSelectedIndices()) {
				if (jlist.getModel().getElementAt(i) == overlayInfo) {
					jlist.removeSelectionInterval(i, i);
				}
			}
		}
		*/
		int[] selections = ovrSrv.getOverlayInfo().selectedIndices();
		jlist.setSelectedIndices(selections);
		selecting = false;
	}

	/*
	// TODO - this may not be best way to do this
	//   Its here to allow acceleration without ALT/OPTION key
	//   Maybe make buttons listen for actions
	@EventHandler
	protected void onKeyPressedEvent(KyPressedEvent ev) {
		System.out.println("key press registered to display "+ev.getDisplay());
		altDown = ev.getModifiers().isAltDown() || ev.getModifiers().isAltGrDown();
		shiftDown = ev.getModifiers().isShiftDown();
		KeyCode key = ev.getCode();
		if (key == KeyCode.T) add();
		if (key == KeyCode.F) flatten();
		if (key == KeyCode.DELETE) delete();
	}
	*/
	
	@SuppressWarnings("unused")
	@EventHandler
	protected void onEvent(OverlayRestructuredEvent event) {
		//System.out.println("restructured");
		jlist.updateUI();
	}

	@SuppressWarnings("unused")
	@EventHandler
	protected void onEvent(OverlayUpdatedEvent event) {
		//System.out.println("updated");
		jlist.updateUI();
	}

	// -- private helpers that implement overlay interaction commands --
	
	/* no longer supported
	private void add() {
		final ImageDisplayService ids = context.getService(ImageDisplayService.class);
		final ImageDisplay activeDisplay = ids.getActiveImageDisplay();
		if (activeDisplay == null) return;
		final List<DataView> views = activeDisplay;
		boolean additions = false;
		for (DataView view : views) {
			if (view.isSelected() && (view instanceof OverlayView))
				additions |= infoList.addOverlay((Overlay)view.getData());
		}
		if (additions)
			jlist.updateUI();
	}
	*/
	
	private void addParticles() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void and() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void delete() {
		if (ovrSrv.getOverlayInfo().getOverlayInfoCount() == 0) return;
		List<Overlay> overlaysToDelete = new LinkedList<Overlay>();
		final int[] selectedIndices = ovrSrv.getOverlayInfo().selectedIndices();
		if (selectedIndices.length == 0) {
			final int result =
				JOptionPane.showConfirmDialog(
					this, "Delete all overlays?", "Delete All", JOptionPane.YES_NO_OPTION);
			if (result != JOptionPane.YES_OPTION) return;
			for (int i = 0; i < ovrSrv.getOverlayInfo().getOverlayInfoCount(); i++) {
				overlaysToDelete.add(ovrSrv.getOverlayInfo().getOverlayInfo(i).getOverlay());
			}
		}
		else {
			for (int i = 0; i < selectedIndices.length; i++) {
				int index = selectedIndices[i];
				overlaysToDelete.add(ovrSrv.getOverlayInfo().getOverlayInfo(index).getOverlay());
			}
		}
		for (Overlay overlay : overlaysToDelete) {
			// NB - removeOverlay() can indirectly change our infoList contents.
			// Thus we first collect overlays from the infoList and then delete
			// them all afterwards to avoid interactions.
			ovrSrv.removeOverlay(overlay);
		}
	}
	
	private void deselect() {
		ovrSrv.getOverlayInfo().deselectAll();
		jlist.clearSelection();
	}
	
	private void draw() {
		OverlayService os = context.getService(OverlayService.class);
		ChannelCollection channels = getChannels();
		List<Overlay> selected = ovrSrv.getOverlayInfo().selectedOverlays();
		for (Overlay o : selected) {
			ImageDisplay disp = os.getFirstDisplay(o);
			os.drawOverlay(o, disp, channels);
		}
	}
	
	private void fill() {
		OverlayService os = context.getService(OverlayService.class);
		ChannelCollection channels = getChannels();
		List<Overlay> selected = ovrSrv.getOverlayInfo().selectedOverlays();
		for (Overlay o : selected) {
			ImageDisplay disp = os.getFirstDisplay(o);
			os.fillOverlay(o, disp, channels);
		}
	}
	
	/* FIXME TODO notes
	 * I'm pretty sure this just snapshots the current view and returns a new
	 * Dataset/Img that is one dimensional, usually rgb, and shows drawn rois.
	 * If show all is true it draws all rois, else it draws curr roi. Note that
	 * we can get rid of show all I think because we always show all. Is that a
	 * problem? Edit mode renders differently. This might affect OverlayService
	 * for drawOverlay() and fillOverlay(). Do we need a way to show single
	 * overlays now? Probably. More hints for OverlayService I suppose. Anyhow
	 * we should be able to grab the current view's (or the active overlay's
	 * view's) ARGBScreenImage and create a new Dataset from it. We could make a
	 * capture() method to ImageDisplay. Right now this implementation does not
	 * show the ROI outline. Thats all the special case code the ROI Mgr flatten
	 * does compared to the menu command flatten. We need a way to tell the
	 * OverlayService to draw an Overlay in a given Dataset. But we want to draw
	 * using fg/bg sometimes and using default drawing capabilities sometimes
	 * (like JHotDraw's rendering). Ideally capture window exactly as is but
	 * without mouse cursor. Right?
	 */
	private void flatten() {
		/*
		List<Overlay> selOverlays = infoList.selectedOverlays();
		if (selOverlays.size() == 0) {
			JOptionPane.showMessageDialog(this, "At least one overlay must be selected");
			return;
		}
		*/
		final ImageDisplayService ids = context.getService(ImageDisplayService.class);
		final ImageDisplay imageDisplay = ids.getActiveImageDisplay();
		if (imageDisplay == null) return;
		final DatasetView view = ids.getActiveDatasetView(imageDisplay);
		ARGBScreenImage screenImage = view.getScreenImage();
		long[] dims = new long[3];
		screenImage.dimensions(dims);  // fill X & Y
		dims[2] = 3;  // fill Z
		if (dims[0] * dims[1] > Integer.MAX_VALUE)
			throw new IllegalArgumentException("image is too big to fit into memory");
		int xSize = (int) dims[0];
		int ySize = (int) dims[1];
		int[] argbPixels = view.getScreenImage().getData();
		DatasetService dss = context.getService(DatasetService.class);
		String newName = "Flattened - "+imageDisplay.getName();
		Dataset dataset = dss.create(new UnsignedByteType(), dims, newName, new Axes[]{Axes.X, Axes.Y, Axes.CHANNEL});
		ImgPlus<? extends RealType<?>> imgPlus = dataset.getImgPlus();
		RandomAccess<? extends RealType<?>> accessor = imgPlus.randomAccess();
		for (int x = 0; x < xSize; x++) {
			accessor.setPosition(x, 0);
			for (int y = 0; y < ySize; y++) {
				accessor.setPosition(y, 1);
				int index = y*xSize + x; 
				int pixel = argbPixels[index];
				accessor.setPosition(0, 2);
				accessor.get().setReal((pixel >> 16) & 0xff);
				accessor.setPosition(1, 2);
				accessor.get().setReal((pixel >>  8) & 0xff);
				accessor.setPosition(2, 2);
				accessor.get().setReal((pixel >>  0) & 0xff);
			}
		}
		dataset.setRGBMerged(true);
		DisplayService ds = context.getService(DisplayService.class);
		ds.createDisplay(newName, dataset);
		// TODO - This currently does not show the ROI outline
	}
	
	private void help() {
		Log.warn("TODO in SwingOverlayManager::help() - using old IJ1 URL for this command");
		final PlatformService ps = context.getService(PlatformService.class);
		try {
			final URL url =
					new URL("http://imagej.nih.gov/ij/docs/menus/analyze.html#manager");
			ps.open(url);
		} catch (IOException e) {
			// do nothing
		}
	}
	
	private void measure() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void multiMeasure() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void multiPlot() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void open() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void options() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void or() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void properties() {
		int[] selected = ovrSrv.getOverlayInfo().selectedIndices();
		if (selected.length == 0) {
			JOptionPane.showMessageDialog(this, "This command requires one or more selections");
			return;
		}
		// else one or more selections exist
		runPropertiesPlugin();
	}
	
	private void removeSliceInfo() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void rename() {
		final int[] selectedIndices = ovrSrv.getOverlayInfo().selectedIndices();
		if (selectedIndices.length < 1) {
			JOptionPane.showMessageDialog(this, "Must select an overlay to rename");
			return;
		}
		if (selectedIndices.length > 1) {
			JOptionPane.showMessageDialog(this, "Cannot rename multiple overlays simultaneously");
			return;
		}
		final OverlayInfo info = ovrSrv.getOverlayInfo().getOverlayInfo(selectedIndices[0]);
		if (info == null) return;
		// TODO - UI agnostic way here
		final String name = JOptionPane.showInputDialog(this, "Enter new name for overlay");
		if ((name == null) || (name.length() == 0))
			info.getOverlay().setName(null);
		else
			info.getOverlay().setName(name);
		jlist.updateUI();
	}
	
	private void save() {
		JOptionPane.showMessageDialog(this, "unimplemented");
		/*
		final int[] selectedIndices = jlist.getSelectedIndices();
		// nothing selected
		if (selectedIndices.length == 0) {
			JOptionPane.showMessageDialog(this, "Cannot save - one or more overlays must be selected first");
			return;
		}
		
		final JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save Overlay to file ...");
		chooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter1, filter2;
		filter1 = new FileNameExtensionFilter("ImageJ overlay containers (*.zip)", "zip");
		chooser.addChoosableFileFilter(filter1);
		filter2 = new FileNameExtensionFilter("ImageJ overlay files (*.ovl)", "ovl");
		chooser.addChoosableFileFilter(filter2);
		chooser.setFileFilter(filter2);
		int result = chooser.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) return;
		String basename = chooser.getSelectedFile().getAbsolutePath();
		if (basename.toLowerCase().endsWith(".ovl") ||
				basename.toLowerCase().endsWith(".zip")) {
			basename = basename.substring(0,basename.length()-4);
		}
		
		// one roi selected
		if (selectedIndices.length == 1) {
			// save overlay in its own user named .ovl file
			String filename = basename + ".ovl";
			final OverlayInfo info = (OverlayInfo) jlist.getSelectedValue();
			//info.overlay.save(filename);
		}
		else { // more than one roi selected
			// save each overlay in its own .ovl file in a user named .zip container
			String filename = basename + ".zip";
			JOptionPane.showMessageDialog(this, "save multiple overlays to zip is unimplemented");
		}
		*/
	}
	
	private void sort() {
		ovrSrv.getOverlayInfo().sort();
		int[] newSelections = ovrSrv.getOverlayInfo().selectedIndices();
		jlist.setSelectedIndices(newSelections);
		jlist.updateUI();
	}
	
	private void specify() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	private void split() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	/*
	 *  old functionality : now that all overlays always tracked this no longer
	 * makes sense
	 *
	// replace OverlayInfoList's currently selected info with the currently
	// selected roi
	private void update() {
		final int[] selectedIndices = infoList.selectedIndices();
		if (selectedIndices.length != 1) {
			JOptionPane.showMessageDialog(this,
				"Exactly one item must be selected");
			return;
		}
		
		final Overlay overlay = getActiveOverlay();
		if (overlay == null) {
			JOptionPane.showMessageDialog(this,
				"An overlay must be selected in the current view");
			return;
		}
		
		final int index = infoList.findIndex(overlay);
		if (index != -1) {
			// already in list
			if (index != selectedIndices[0])
				JOptionPane.showMessageDialog(this,
					"Selected overlay is already tracked by the overlay manager");
			return;
		}

		infoList.replaceOverlay(selectedIndices[0], overlay);
		jlist.updateUI();
	}
	 */
	
	private void xor() {
		JOptionPane.showMessageDialog(this, "unimplemented");
	}
	
	// -- private helpers for hotkey handling --

	private void setupKeyListener() {
		//KeyListener listener = new AWTKeyEventDispatcher(fakeDisplay, eventService);
		final KeyListener listener = new KeyListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void keyPressed(KeyEvent e) {
				altDown = e.isAltDown() || e.isAltGraphDown();
				shiftDown = e.isShiftDown();
				/* no longer supported
				if (e.getKeyCode() == KeyEvent.VK_T) add();
				*/
				if (e.getKeyCode() == KeyEvent.VK_F) flatten();
				if (e.getKeyCode() == KeyEvent.VK_DELETE) delete();
			}
			@Override
			@SuppressWarnings("synthetic-access")
			public void keyReleased(KeyEvent e) {
				altDown = e.isAltDown() || e.isAltGraphDown();
				shiftDown = e.isShiftDown();
			}
			@Override
			public void keyTyped(KeyEvent e) { /* do nothing */ }
		};
		
		final Stack<Component> stack = new Stack<Component>();
		stack.push(this);
		while (!stack.empty()) {
			final Component component = stack.pop();
			component.addKeyListener(listener);
			if (component instanceof Container) {
				final Container container = (Container) component;
				for (Component c : container.getComponents())
					stack.push(c);
			}
		}
	}

	// -- private helpers for frame location --
	
	/** Persists the application frame's current location. */
	private void saveLocation() {
		Prefs.put(getClass(), LAST_X, getLocation().x);
		Prefs.put(getClass(), LAST_Y, getLocation().y);
	}

	/** Restores the application frame's current location. */
	private void restoreLocation() {
		final int lastX = Prefs.getInt(getClass(), LAST_X, 0);
		final int lastY = Prefs.getInt(getClass(), LAST_Y, 0);
		setLocation(lastX, lastY);
	}
	
	private void setupCloseListener() {
		addWindowListener(new WindowAdapter() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void windowClosing(WindowEvent e) {
				// Remember screen location of window for next time
				saveLocation();
			}
		});
	}

	// -- private helpers for list selection event listening --

	private void setupListSelectionListener() {
		final ListSelectionListener
			listSelectionListener =	new ListSelectionListener() {
				@Override
				@SuppressWarnings("synthetic-access")
				public void valueChanged(final ListSelectionEvent listSelectionEvent) {
					if (selecting) return;
					selecting = true;
					final ImageDisplayService imageDisplayService =
						context.getService(ImageDisplayService.class);
					final ImageDisplay display =
						imageDisplayService.getActiveImageDisplay();
					if (display == null) return;
					final JList list = (JList) listSelectionEvent.getSource();
					final Object[] selectionValues = list.getSelectedValues();
					ovrSrv.getOverlayInfo().deselectAll();
					for (final Object overlayInfoObj : selectionValues) {
						final OverlayInfo overlayInfo = (OverlayInfo) overlayInfoObj;
						overlayInfo.setSelected(true);
					}
					for (final DataView overlayView : display) {
						overlayView.setSelected(false);
						for (final Object overlayInfoObj : selectionValues) {
							final OverlayInfo overlayInfo = (OverlayInfo) overlayInfoObj;
							if (overlayInfo.getOverlay() == overlayView.getData()) {
								overlayInfo.setSelected(true);
								overlayView.setSelected(true);
								break;
							}
						}
					}
					selecting = false;
				}
			};
		jlist.addListSelectionListener(listSelectionListener);
	}

	// -- private helpers for constructing popup menu --
	
	private JPopupMenu getPopupMenu() {
		if (popupMenu == null)
			popupMenu = createPopupMenu();
		return popupMenu;
	}

	private JPopupMenu createPopupMenu() {
		final JPopupMenu menu = new JPopupMenu();
		menu.add(getOpenMenuItem());
		menu.add(getSaveMenuItem());
		menu.add(getAndMenuItem());
		menu.add(getOrMenuItem());
		menu.add(getXorMenuItem());
		menu.add(getSplitMenuItem());
		menu.add(getAddParticlesMenuItem());
		menu.add(getMultiMeasureMenuItem());
		menu.add(getMultiPlotMenuItem());
		menu.add(getSortMenuItem());
		menu.add(getSpecifyMenuItem());
		menu.add(getRemoveSliceInfoMenuItem());
		menu.add(getHelpMenuItem());
		menu.add(getOptionsMenuItem());
		return menu;
	}
	
	private JMenuItem getAddParticlesMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Add Particles");
		item.setActionCommand(ACTION_ADD_PARTICLES);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getAndMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("AND");
		item.setActionCommand(ACTION_AND);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getHelpMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Help");
		item.setActionCommand(ACTION_HELP);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getMultiMeasureMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Multi Measure");
		item.setActionCommand(ACTION_MULTI_MEASURE);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getMultiPlotMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Multi Plot");
		item.setActionCommand(ACTION_MULTI_PLOT);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getOpenMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Open...");
		item.setActionCommand(ACTION_OPEN);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getOptionsMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Options...");
		item.setActionCommand(ACTION_OPTIONS);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getOrMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("OR (Combine)");
		item.setActionCommand(ACTION_OR);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getRemoveSliceInfoMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Remove Slice Info");
		item.setActionCommand(ACTION_REMOVE_SLICE_INFO);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getSaveMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Save...");
		item.setActionCommand(ACTION_SAVE);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getSortMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Sort");
		item.setActionCommand(ACTION_SORT);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getSpecifyMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Specify...");
		item.setActionCommand(ACTION_SPECIFY);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getSplitMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("Split");
		item.setActionCommand(ACTION_SPLIT);
		item.addActionListener(this);
		return item;
	}
	
	private JMenuItem getXorMenuItem() {
		final JMenuItem item;
		item = new JMenuItem("XOR");
		item.setActionCommand(ACTION_XOR);
		item.addActionListener(this);
		return item;
	}
	
	// -- private helpers to implement main pane button controls --

	/* no longer supported
	private JButton getAddButton() {
		final JButton button = new JButton("Add [t]");
		button.setActionCommand(ACTION_ADD);
		button.addActionListener(this);
		return button;
	}
	*/
	
	private JButton getDeleteButton() {
		final JButton button = new JButton("Delete");
		button.setActionCommand(ACTION_DELETE);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getDeselectButton() {
		final JButton button = new JButton("Deselect");
		button.setActionCommand(ACTION_DESELECT);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getDrawButton() {
		final JButton button = new JButton("Draw");
		button.setActionCommand(ACTION_DRAW);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getFillButton() {
		final JButton button = new JButton("Fill");
		button.setActionCommand(ACTION_FILL);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getFlattenButton() {
		final JButton button = new JButton("Flatten [f]");
		button.setActionCommand(ACTION_FLATTEN);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getMeasureButton() {
		final JButton button = new JButton("Measure");
		button.setActionCommand(ACTION_MEASURE);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getMoreButton() {
		final JButton button = new JButton("More "+'\u00bb');
		button.addMouseListener(new MouseListener() {
			@Override
			@SuppressWarnings("synthetic-access")
			public void mouseClicked(MouseEvent e) {
				getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			}
			@Override
			public void mouseEntered(MouseEvent evt) { /* do nothing */ }
			@Override
			public void mouseExited(MouseEvent evt) { /* do nothing */ }
			@Override
			public void mousePressed(MouseEvent evt) { /* do nothing */ }
			@Override
			public void mouseReleased(MouseEvent evt) { /* do nothing */ }
			
		});
		return button;
	}
	
	private JButton getPropertiesButton() {
		final JButton button = new JButton("Properties...");
		button.setActionCommand(ACTION_PROPERTIES);
		button.addActionListener(this);
		return button;
	}
	
	private JButton getRenameButton() {
		final JButton button = new JButton("Rename...");
		button.setActionCommand(ACTION_RENAME);
		button.addActionListener(this);
		return button;
	}

	/* no longer supported
	private JButton getUpdateButton() {
		final JButton button = new JButton("Update");
		button.setActionCommand(ACTION_UPDATE);
		button.addActionListener(this);
		return button;
	}
	*/
	
	// -- private helpers to change state when checkboxes change --

	// TODO
	
	@Override
	public void itemStateChanged(ItemEvent evt) {
		final boolean selected = (evt.getStateChange() == ItemEvent.SELECTED);
		if (evt.getSource() == showAllCheckBox) {
			//System.out.println("show all is now "+selected);
		}
		if (evt.getSource() == editModeCheckBox) {
			//System.out.println("edit mode is now "+selected);
			// link both checkboxes in selected case
			if (selected)
				showAllCheckBox.setSelected(true);
		}
	}

	// -- private helpers for TODO XXXX --
	
	// TODO - assumes first selected overlay view is the only one. bad?
	@SuppressWarnings("unused")
	private Overlay getActiveOverlay() {
		final ImageDisplayService ids = context.getService(ImageDisplayService.class);
		final ImageDisplay activeDisplay = ids.getActiveImageDisplay();
		if (activeDisplay == null) return null;
		final List<DataView> views = activeDisplay;
		for (DataView view : views) {
			if (view.isSelected() && (view instanceof OverlayView))
				return ((OverlayView) view).getData();
		}
		return null;
	}
	
	private void runPropertiesPlugin() {
		final Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("overlays", ovrSrv.getOverlayInfo().selectedOverlays());
		PluginService pluginService = context.getService(PluginService.class);
		pluginService.run(SelectedManagerOverlayProperties.class, inputMap);
		/*
		String name = info.toString();
		ColorRGB lineColor = info.overlay.getLineColor();
		ColorRGB fillColor = info.overlay.getFillColor();
		double width = info.overlay.getLineWidth();
		JOptionPane.showMessageDialog(this,
			"(" + name +
			") (" + lineColor +
			") (" + fillColor +
			") (" + width + ")");
		*/
	}

	private ChannelCollection getChannels() {
		final OptionsService oSrv = context.getService(OptionsService.class);
		OptionsChannels opts = oSrv.getOptions(OptionsChannels.class);
		if (altDown) return opts.getBgValues();
		return opts.getFgValues();
	}
}
