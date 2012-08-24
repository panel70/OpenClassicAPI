package ch.spacebase.openclassic.api.gui.widget;

import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.gui.GuiScreen;
import ch.spacebase.openclassic.api.render.RenderHelper;

/**
 * A widget that displays a texture.
 */
public class Image extends Widget {

	private SubTexture tex;
	
	public Image(int id, int x, int y, GuiScreen parent, SubTexture tex) {
		super(id, x, y, Math.abs(tex.getX2() - tex.getX1()), Math.abs(tex.getY2() - tex.getY1()), parent);
	}
	
	/**
	 * Gets the texture being displayed.
	 * @return The texture being displayed.
	 */
	public SubTexture getTexture() {
		return this.tex;
	}
	
	/**
	 * Sets the texture being displayed.
	 * @param tex The new texture being displayed.
	 */
	public void setTexture(SubTexture tex) {
		this.tex = tex;
		this.width = Math.abs(tex.getX2() - tex.getX1());
		this.height = Math.abs(tex.getY2() - tex.getY1());
		
	}

	@Override
	public void render() {
		RenderHelper.getHelper().drawSubTex(this.tex, this.x, this.y);
	}

}