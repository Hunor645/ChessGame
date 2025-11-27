package org.yourcompany.yourproject;

import java.awt.event.MouseAdapter;

public class Mouse extends MouseAdapter{
    int x,y;
    public boolean pressed;

    @Override
    public void mousePressed(java.awt.event.MouseEvent e) {
        pressed = true;
    }

    @Override 
    public void mouseReleased(java.awt.event.MouseEvent e) {
        pressed = false;
    }

    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }

    @Override
    public void mouseDragged(java.awt.event.MouseEvent e) {
        x = e.getX();
        y = e.getY();
    }

}