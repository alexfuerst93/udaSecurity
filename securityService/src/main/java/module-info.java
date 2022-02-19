module com.udacity.catpoint.securityService {
    requires com.udacity.catpoint.imageService;
    requires java.desktop;
    requires miglayout;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    opens com.udacity.catpoint.security.data to com.google.gson;
    opens com.udacity.catpoint.security.service;
}