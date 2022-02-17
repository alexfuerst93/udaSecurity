package com.udacity.catpoint.image.service;

import java.awt.image.BufferedImage;

public interface FakeImage {

    boolean imageContainsCat(BufferedImage image, float confidenceThreshhold);

}
