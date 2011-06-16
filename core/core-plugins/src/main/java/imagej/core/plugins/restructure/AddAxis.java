package imagej.core.plugins.restructure;

//
//AddAxis.java
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

import net.imglib2.img.Axis;
import net.imglib2.img.ImgPlus;
import net.imglib2.type.numeric.RealType;
import imagej.data.Dataset;
import imagej.plugin.ImageJPlugin;
import imagej.plugin.Menu;
import imagej.plugin.Plugin;
import imagej.plugin.Parameter;


/**
* Adds a new axis to an input Dataset
* 
* @author Barry DeZonia
*/
@Plugin(menu = {
@Menu(label = "Image", mnemonic = 'i'),
@Menu(label = "Stacks", mnemonic = 's'),
@Menu(label = "Add Axis") })
public class AddAxis implements ImageJPlugin {
	
	@Parameter(required = true)
	private Dataset input;

	// TODO - populate choices from Dataset somehow
	@Parameter(label="Axis to add",choices = {
		RestructureUtils.X,
		RestructureUtils.Y,
		RestructureUtils.Z,
		RestructureUtils.CH,
		RestructureUtils.TI,
		RestructureUtils.FR,
		RestructureUtils.SP,
		RestructureUtils.PH,
		RestructureUtils.PO,
		RestructureUtils.LI})
	private String axisToAdd;
	
	@Parameter(label="Axis size", min="1")
	private long axisSize;

	/** creates new ImgPlus data with an additonal axis. sets pixels of 1st
	 * hyperplane of new imgPlus to original imgPlus data. Assigns the ImgPlus
	 * to the input Dataset.
	 */
	@Override
	public void run() {
		Axis axis = RestructureUtils.getAxis(axisToAdd);
		if (inputBad(axis)) return;
		Axis[] newAxes = getNewAxes(input, axis);
		long[] newDimensions = getNewDimensions(input, axisSize);
		ImgPlus<? extends RealType<?>> dstImgPlus =
			RestructureUtils.createNewImgPlus(input, newDimensions, newAxes);
		fillNewImgPlus(input.getImgPlus(), dstImgPlus);
		// TODO - colorTables, metadata, etc.?
		input.setImgPlus(dstImgPlus);
	}
	
	/** detects if user specified data is invalid */
	private boolean inputBad(Axis axis) {
		// axis not determined by dialog
		if (axis == null)
			return true;
		
	  // axis already present in Dataset
		int axisIndex = input.getAxisIndex(axis);
		if (axisIndex >= 0)
			return true;
		
		// axis size invalid
		if (axisSize <= 0)
			return true;
		
		return false;
	}

	/** creates an Axis[] that consists of all the axes from a Dataset and an
	 * additional axis appended.
	 */
	private Axis[] getNewAxes(Dataset ds, Axis axis) {
		Axis[] origAxes = ds.getAxes();
		Axis[] newAxes = new Axis[origAxes.length+1];
		for (int i = 0; i < origAxes.length; i++)
			newAxes[i] = origAxes[i];
		newAxes[newAxes.length-1] = axis;
		return newAxes;
	}
	
	/** creates a long[] that consists of all the dimensions from a Dataset and
	 * an additional value appended.
	 */
	private long[] getNewDimensions(Dataset ds, long lastDimensionSize) {
		long[] origDims = ds.getDims();
		long[] newDims = new long[origDims.length+1];
		for (int i = 0; i < origDims.length; i++)
			newDims[i] = origDims[i];
		newDims[newDims.length-1] = lastDimensionSize;
		return newDims;
	}
	
	/** fills the 1st hyperplane in the new Dataset with the entire contents
	 * of the original image
	 */
	private void fillNewImgPlus(ImgPlus<? extends RealType<?>> srcImgPlus,
		ImgPlus<? extends RealType<?>> dstImgPlus)
	{
		long[] srcOrigin = new long[srcImgPlus.numDimensions()];
		long[] dstOrigin = new long[dstImgPlus.numDimensions()];
		
		long[] srcSpan = new long[srcOrigin.length];
		long[] dstSpan = new long[dstOrigin.length];

		srcImgPlus.dimensions(srcSpan);
		dstImgPlus.dimensions(dstSpan);
		dstSpan[dstSpan.length-1] = 1;
		
		RestructureUtils.copyHyperVolume(srcImgPlus, srcOrigin, srcSpan, dstImgPlus, dstOrigin, dstSpan);
	}
}