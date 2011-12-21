/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DMSTech;

/**
 *
 * @author ijrikgnmd
 */
public class canvas {

    public String getCanvas() {
        return canvas;
    }

    public ImageChoice[] getImageURL() {
        return imageURL;
    }

    public int getPosition() {
        return position;
    }

    public String getTitle() {
        return title;
    }
    private String canvas;
    private String title;
    private ImageChoice[] imageURL;
    private int position;
    public canvas(String canvas, String title, ImageChoice[] img,int position)
    {
        this.canvas=canvas;
        this.title=title;
        this.imageURL=img;
        this.position=position;
    }
}
