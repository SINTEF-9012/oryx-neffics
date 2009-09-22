//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.09.07 at 02:19:19 PM CEST 
//


package de.hpi.bpmn2_0.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import de.hpi.bpmn2_0.model.connector.MessageFlow;
import de.hpi.bpmn2_0.model.participant.Participant;


/**
 * <p>Java class for tCollaboration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tCollaboration">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tRootElement">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}participant" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}messageFlow" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}artifact" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}conversation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}conversationAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}participantAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/bpmn20}messageFlowAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="isClosed" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="choreographyRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tCollaboration", propOrder = {
    "participant",
    "messageFlow",
    "artifact",
    "conversation",
    "conversationAssociation",
    "participantAssociation",
    "messageFlowAssociation"
})
public class Collaboration
    extends RootElement
{

    protected List<Participant> participant;
    protected List<MessageFlow> messageFlow;
//    @XmlElementRef(name = "artifact", namespace = "http://www.omg.org/bpmn20", type = JAXBElement.class)
//    protected List<JAXBElement<? extends TArtifact>> artifact;
//    protected List<tConversation> conversation;
//    protected List<TConversationAssociation> conversationAssociation;
//    protected List<TParticipantAssociation> participantAssociation;
//    protected List<TMessageFlowAssociation> messageFlowAssociation;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Boolean isClosed;
    @XmlAttribute
    protected QName choreographyRef;

    /**
     * Gets the value of the participant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the participant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParticipant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TParticipant }
     * 
     * 
     */
//    public List<TParticipant> getParticipant() {
//        if (participant == null) {
//            participant = new ArrayList<TParticipant>();
//        }
//        return this.participant;
//    }

    /**
     * Gets the value of the messageFlow property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageFlow property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageFlow().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TMessageFlow }
     * 
     * 
     */
//    public List<TMessageFlow> getMessageFlow() {
//        if (messageFlow == null) {
//            messageFlow = new ArrayList<TMessageFlow>();
//        }
//        return this.messageFlow;
//    }

    /**
     * Gets the value of the artifact property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the artifact property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArtifact().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link TArtifact }{@code >}
     * {@link JAXBElement }{@code <}{@link TAssociation }{@code >}
     * {@link JAXBElement }{@code <}{@link TGroup }{@code >}
     * {@link JAXBElement }{@code <}{@link TTextAnnotation }{@code >}
     * 
     * 
     */
//    public List<JAXBElement<? extends TArtifact>> getArtifact() {
//        if (artifact == null) {
//            artifact = new ArrayList<JAXBElement<? extends TArtifact>>();
//        }
//        return this.artifact;
//    }

    /**
     * Gets the value of the conversation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conversation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConversation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TConversation }
     * 
     * 
     */
//    public List<TConversation> getConversation() {
//        if (conversation == null) {
//            conversation = new ArrayList<TConversation>();
//        }
//        return this.conversation;
//    }

    /**
     * Gets the value of the conversationAssociation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conversationAssociation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConversationAssociation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TConversationAssociation }
     * 
     * 
     */
//    public List<TConversationAssociation> getConversationAssociation() {
//        if (conversationAssociation == null) {
//            conversationAssociation = new ArrayList<TConversationAssociation>();
//        }
//        return this.conversationAssociation;
//    }

    /**
     * Gets the value of the participantAssociation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the participantAssociation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParticipantAssociation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TParticipantAssociation }
     * 
     * 
     */
//    public List<TParticipantAssociation> getParticipantAssociation() {
//        if (participantAssociation == null) {
//            participantAssociation = new ArrayList<TParticipantAssociation>();
//        }
//        return this.participantAssociation;
//    }

    /**
     * Gets the value of the messageFlowAssociation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageFlowAssociation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageFlowAssociation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TMessageFlowAssociation }
     * 
     * 
     */
//    public List<TMessageFlowAssociation> getMessageFlowAssociation() {
//        if (messageFlowAssociation == null) {
//            messageFlowAssociation = new ArrayList<TMessageFlowAssociation>();
//        }
//        return this.messageFlowAssociation;
//    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the isClosed property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsClosed() {
        if (isClosed == null) {
            return false;
        } else {
            return isClosed;
        }
    }

    /**
     * Sets the value of the isClosed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsClosed(Boolean value) {
        this.isClosed = value;
    }

    /**
     * Gets the value of the choreographyRef property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getChoreographyRef() {
        return choreographyRef;
    }

    /**
     * Sets the value of the choreographyRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setChoreographyRef(QName value) {
        this.choreographyRef = value;
    }

}
