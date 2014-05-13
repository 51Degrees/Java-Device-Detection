/* *********************************************************************
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0.
 * 
 * If a copy of the MPL was not distributed with this file, You can obtain
 * one at http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 * ********************************************************************* */
package fiftyone.mobile.detection;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Describes a property which can be assigned to a device.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class Property extends PropertyValue {

    /**
     * A list of possible values the property can have.
     */
    final private List<Value> _values = new ArrayList<Value>();
    /**
     * Indicates if the property is a list e.g. multiple values
     */
    private boolean _isList = false;
    /**
     * Indicates if property is mandatory and should be returned for every
     * device.
     */
    private boolean _isMandatory = false;
    /**
     * Indicates if the values associated with the property are suitable to be
     * displayed.
     */
    private boolean _showValues = false;
    /**
     * Returns the type of the component the property relates to. Values include;
     * Hardware, Software, Browser and Crawler. 
     */
    private Components _component = Components.Unknown;
    
    /**
     * Constructs an instance of Property.
     *
     * @param provider The provider the property was created from.
     * @param name The String name.
     */
    Property(final Provider provider, final String name) {

        super(provider, name);
    }

    /**
     * Constructs an instance of Property.
     *
     * @param provider The provider the property was created from.
     * @param name The String name.
     * @param description The description of the name.
     */
    Property(
            final Provider provider, 
            final String name, 
            final String description) {
        super(provider, name, description);
    }

    /**
     * Constructs an instance of Property.
     *
     * @param provider The provider the property was created from.
     * @param name The String name.
     * @param description The description of the name.
     * @param url An optional URL linking to more information about the name.
     */
    Property(
            final Provider provider, 
            final String name, 
            final String description, 
            final String url) {
        super(provider, name, description, url);
    }

    /**
     * Constructs an instance of Property.
     *
     * @param provider The provider the property was created from.
     * @param name The String name.
     * @param description The description of the name.
     * @param url An optional URL linking to more information about the name.
     * @param isMandatory Should it be returned for every device?
     * @param isList Is it a list?
     * @param showValues Are values suitable to be displayed?
     */
    public Property(
            final Provider provider, 
            final String name, 
            final String description, 
            final String url,
            final boolean isMandatory, 
            final boolean isList, 
            final boolean showValues) {
        super(provider, name, description, url);
        _isMandatory = isMandatory;
        _isList = isList;
        _showValues = showValues;
    }

    /**
     * Sets the value of the component the property relates to. Used 
     * by the extension to the data structures to include component
     * information in the data file.
     * 
     * @param component the type of component the property relates to.
     */
    public void setComponent(final Components component) {
        _component = component;
    }    
    
    /**
     * 
     * @return the component the property relates to.
     */
    public Components getComponent() {
        return _component;
    }
    
    /**
     *
     * @return A list of possible values the property can have.
     */
    public List<Value> getValues() {
        return _values;
    }

    /**
     *
     * @return true if the property is mandatory and should be returned for
     * every device.
     */
    public boolean getIsMandatory() {
        return _isMandatory;
    }

    /**
     *
     * @return Returns true if the property is a list type and multiple values
     * will be returned.
     */
    public boolean isList() {
        return _isList;
    }

    /**
     *
     * @return Returns true if the values associated with the property are
     * suitable to be displayed. Will return false if they're numeric and do not
     * generally present well when shown as a list.
     */
    public boolean getShowValues() {
        return _showValues;
    }
}