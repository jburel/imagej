//
// EllipseAdapter.java
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

package imagej.ui.swing.tools.overlay;

import imagej.data.display.ImageDisplay;
import imagej.data.display.OverlayView;
import imagej.data.overlay.EllipseOverlay;
import imagej.data.overlay.Overlay;
import imagej.ext.plugin.Plugin;
import imagej.ext.tool.Tool;
import imagej.ui.swing.overlay.IJCreationTool;
import imagej.ui.swing.overlay.JHotDrawOverlayAdapter;
import imagej.ui.swing.overlay.JHotDrawTool;
import imagej.ui.swing.overlay.OverlayCreatedListener;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import net.imglib2.RealPoint;
import net.imglib2.roi.EllipseRegionOfInterest;

import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.EllipseFigure;
import org.jhotdraw.draw.Figure;

/**
 * TODO
 * 
 * @author Lee Kamentsky
 * @author Barry DeZonia
 */
@Plugin(type = Tool.class, name = "Oval", description = "Oval selections",
	iconPath = "/icons/tools/oval.png", priority = EllipseAdapter.PRIORITY,
	enabled = true)
@JHotDrawOverlayAdapter(priority = EllipseAdapter.PRIORITY)
public class EllipseAdapter extends
	AbstractJHotDrawOverlayAdapter<EllipseOverlay, EllipseFigure>
{

	public static final int PRIORITY = RectangleAdapter.PRIORITY - 1;

	static protected EllipseOverlay downcastOverlay(final Overlay roi) {
		assert (roi instanceof EllipseOverlay);
		return (EllipseOverlay) roi;
	}

	@Override
	public boolean supports(final Overlay overlay, final Figure figure) {
		if ((figure != null) && (!(figure instanceof EllipseFigure))) {
			return false;
		}
		return overlay instanceof EllipseOverlay;
	}

	@Override
	public Overlay createNewOverlay() {
		return new EllipseOverlay(getContext());
	}

	@Override
	public Figure createDefaultFigure() {
		@SuppressWarnings("serial")
		final EllipseFigure figure = new EllipseFigure() {

			// Make sure that the lines are always drawn 1 pixel wide
			@Override
			public void draw(final Graphics2D g) {
				set(AttributeKeys.STROKE_WIDTH, new Double(1 / g.getTransform()
					.getScaleX()));
				super.draw(g);
			}
		};
		figure.set(AttributeKeys.FILL_COLOR, getDefaultFillColor());
		figure.set(AttributeKeys.STROKE_COLOR, getDefaultStrokeColor());
		// Avoid IllegalArgumentException: miter limit < 1 on the EDT
		figure.set(AttributeKeys.IS_STROKE_MITER_LIMIT_FACTOR, false);
		return figure;
	}

	@Override
	public void updateFigure(final OverlayView o, final EllipseFigure figure) {
		super.updateFigure(o, figure);
		final EllipseOverlay overlay = downcastOverlay(o.getData());
		final EllipseRegionOfInterest eRoi = overlay.getRegionOfInterest();
		final double centerX = eRoi.getOrigin(0);
		final double centerY = eRoi.getOrigin(1);
		final double radiusX = eRoi.getRadius(0);
		final double radiusY = eRoi.getRadius(1);

		figure.setBounds(new Point2D.Double(centerX - radiusX, centerY - radiusY),
			new Point2D.Double(centerX + radiusX, centerY + radiusY));
	}

	@Override
	public void updateOverlay(final EllipseFigure figure, final OverlayView o) {
		super.updateOverlay(figure, o);
		final EllipseOverlay overlay = downcastOverlay(o.getData());
		final Rectangle2D.Double r = figure.getBounds();
		final RealPoint ptCenter =
			new RealPoint(new double[] { r.x + r.width / 2, r.y + r.height / 2 });
		final EllipseRegionOfInterest eRoi = overlay.getRegionOfInterest();
		eRoi.setOrigin(ptCenter);
		eRoi.setRadius(r.width / 2, 0);
		eRoi.setRadius(r.height / 2, 1);
	}

	@Override
	public JHotDrawTool getCreationTool(final ImageDisplay display,
		final OverlayCreatedListener listener)
	{
		return new IJCreationTool<EllipseFigure>(display, this, listener);
	}

	@Override
	public Shape toShape(final EllipseFigure figure) {
		Rectangle2D.Double bounds = figure.getBounds();
		return new Ellipse2D.Double(bounds.x, bounds.y, bounds.width, bounds.height);
	}
}
