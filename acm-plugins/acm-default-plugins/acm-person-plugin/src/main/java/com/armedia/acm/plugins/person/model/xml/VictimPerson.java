/**
 * 
 */
package com.armedia.acm.plugins.person.model.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.armedia.acm.objectonverter.adapter.DateFrevvoAdapter;
import com.armedia.acm.plugins.addressable.model.ContactMethod;
import com.armedia.acm.plugins.addressable.model.PostalAddress;
import com.armedia.acm.plugins.addressable.model.xml.VictimContactMethod;
import com.armedia.acm.plugins.addressable.model.xml.VictimPostalAddress;
import com.armedia.acm.plugins.person.model.Person;

/**
 * @author riste.tutureski
 *
 */
public class VictimPerson extends Person implements FrevvoPerson {

	private static final long serialVersionUID = 601307053343844821L;
	
	private String type;
	private String notifiedOutcome;
	private List<String> personIdentificationKeys = Arrays.asList("notifiedOutcome");
	
	public VictimPerson()
	{
		
	}
	
	public VictimPerson(Person person)
	{
		setId(person.getId());
		setGivenName(person.getGivenName());
		setFamilyName(person.getFamilyName());
		setDateOfBirth(person.getDateOfBirth());
		
		if (person.getAddresses() != null)
		{
			List<PostalAddress> addresses = new ArrayList<PostalAddress>();
			for (PostalAddress postalAddress : person.getAddresses())
    		{
				VictimPostalAddress a = new VictimPostalAddress(postalAddress);
    			addresses.add(a);
    		}
			setAddresses(addresses);
		}
		
		if (person.getContactMethods() != null)
        {
        	List<ContactMethod> contactMethods = new ArrayList<ContactMethod>();
        	for (ContactMethod contactMethod : person.getContactMethods())
    		{
        		VictimContactMethod c = new VictimContactMethod(contactMethod);
        		contactMethods.add(c);
    		}
        	setContactMethods(contactMethods);
        }
		
		setPersonIdentification(person.getPersonIdentification());
	}

	@XmlElement(name="victimId")
	@Override
	public Long getId() {
        return super.getId();
    }

	@Override
    public void setId(Long id) {
        super.setId(id);
    }
	
	@XmlElement(name="victimFirstName")
	@Override
	public String getGivenName() {
        return super.getGivenName();
    }

	@Override
    public void setGivenName(String givenName) {
        super.setGivenName(givenName);
    }
	
	@XmlElement(name="victimLastName")
	@Override
	public String getFamilyName() {
        return super.getFamilyName();
    }

	@Override
    public void setFamilyName(String familyName) {
        super.setFamilyName(familyName);
    }

	@XmlElement(name="victimType")
	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	@XmlElement(name="victimDOB")
	@XmlJavaTypeAdapter(value=DateFrevvoAdapter.class)
	public Date getDateOfBirth() {
        return super.getDateOfBirth();
    }

	@Override
    public void setDateOfBirth(Date dateOfBirth) {
        super.setDateOfBirth(dateOfBirth);
    }

	@XmlElement(name="victimLocation", type=VictimPostalAddress.class)
	@Override
	public List<PostalAddress> getAddresses() {
        return super.getAddresses();
    }
	
	@Override
    public void setAddresses(List<PostalAddress> addresses) {
        super.setAddresses(addresses);
    }
	
	@XmlElement(name="victimContact", type=VictimContactMethod.class)
	@Override
	public List<ContactMethod> getContactMethods() {
        return super.getContactMethods();
    }

	@Override
    public void setContactMethods(List<ContactMethod> contactMethods) {
        super.setContactMethods(contactMethods);
    }

	@XmlElement(name="victimNotifiedOutcome")
	public String getNotifiedOutcome() {
		return notifiedOutcome;
	}

	public void setNotifiedOutcome(String notifiedOutcome) {
		this.notifiedOutcome = notifiedOutcome;
	}
	
	@XmlTransient
	@Override
	public List<String> getPersonIdentificationKeys() {
		return personIdentificationKeys;
	}

	public void setPersonIdentificationKeys(List<String> personIdentificationKeys) {
		this.personIdentificationKeys = personIdentificationKeys;
	}

	@Override
	public Person returnBase()
	{
		Person person = new Person();
		
		person.setId(getId());
		person.setGivenName(getGivenName());
		person.setFamilyName(getFamilyName());
		person.setDateOfBirth(getDateOfBirth());
		
		if (getAddresses() != null)
		{
			List<PostalAddress> addresses = new ArrayList<PostalAddress>();
			for (PostalAddress postalAddress : getAddresses())
    		{
    			PostalAddress base = postalAddress.returnBase();
    			addresses.add(base);
    		}
			person.setAddresses(addresses);
		}
		
		if (getContactMethods() != null)
        {
        	List<ContactMethod> contactMethods = new ArrayList<ContactMethod>();
        	for (ContactMethod contactMethod : getContactMethods())
    		{
        		ContactMethod base = contactMethod.returnBase();
        		contactMethods.add(base);
    		}
        	person.setContactMethods(contactMethods);
        }
		
		person.setPersonIdentification(getPersonIdentification());
		
		return person;
	}
	
	
}
