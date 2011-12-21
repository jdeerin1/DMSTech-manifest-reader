/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DMSTech;

/**
 *
 * @author ijrikgnmd
 */
public class ImageChoice {

    public int getHeight() {
        return height;
    }

    public String getImageURL() {
        return imageURL;
    }

    public int getWidth() {
        return width;
    }
    private int height;
    private int width;
    private String imageURL;
    public ImageChoice(String imageURL, int h, int w)
    {
        this.height=h;
        this.width=w;
        this.imageURL=imageURL;
        
    }
    
}
