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

/**
 *
 * Describes a name that can be assigned to a property.
 *
 * @author 51Degrees.mobi
 * @version 2.2.9.1
 */
public class PropertyValue {

    /**
     * The String index of the name.
     */
    private int _nameStringIndex = -1;
    /**
     * The String name.
     */
    private String _name;
    /**
     * A description of the name.
     */
    private String _description;
    /**
     * A url to more information about the name.
     */
    private String _url;
    /**
     * The provider the property or value is associated with.
     */
    private Provider _provider;

    /**
     * Constructs an instance of Property Value.
     *
     * @param provider The provider the property was created from.
     * @param name The String name.
     */
    PropertyValue(final Provider provider, final String name) {
        _provider = provider;
        _name = name;
    }

    /**
     *
     * Constructs an instance of Property Value.
     *
     * @param provider The provider the property was created from.
     * @param name The String name.
     * @param description The description of the name.
     */
    PropertyValue(
            final Provider provider, 
            final String name, 
            final String description) {
        this(provider, name);
        _description = description;
    }

    /**
     *
     * Constructs an instance of Property Value.
     *
     * @param provider The provider the property was created from.
     * @param name The String name.
     * @param description The description of the name.
     * @param url An optional URL linking to more information about the name.
     */
    PropertyValue(
            final Provider provider, 
            final String name, 
            final String description, 
            final String url) {
        this(provider, name, description);
        _url = url;
    }

    /**
     *
     * @return The String index of the name.
     */
    public int getNameStringIndex() {
        if (_nameStringIndex < 0) {
            _nameStringIndex = _provider.getStrings().add(getName());
        }
        return _nameStringIndex;
    }

    /**
     *
     * @return The String name.
     */
    public String getName() {
        return _name;
    }

    /**
     *
     * @return A description of the name.
     */
    public String getDescription() {
        return _description;
    }

    /**
     *
     * @return A url to more information about the name.
     */
    public String getUrl() {
        return _url;
    }

    /**
     *
     * @return The provider the property or value is associated with.
     */
    public Provider getProvider() {
        return _provider;
    }
}
