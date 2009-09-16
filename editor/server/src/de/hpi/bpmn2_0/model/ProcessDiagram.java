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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for processDiagramType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="processDiagramType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://bpmndi.org}diagramType">
 *       &lt;sequence>
 *         &lt;element ref="{http://bpmndi.org}laneCompartment" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://bpmndi.org}sequenceFlowConnector" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://bpmndi.org}associationConnector" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://bpmndi.org}dataAssociationConnector" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="processRef" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(namespace = "http://bpmndi.org", name = "processDiagramType")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processDiagramType", namespace = "http://bpmndi.org", propOrder = {
    "laneCompartment",
    "sequenceFlowConnector"//,
//    "associationConnector",
//    "dataAssociationConnector"
})
public class ProcessDiagram
    extends BpmnDiagram
{

    @XmlElement(namespace = "http://bpmndi.org", required = true)
    protected List<LaneCompartment> laneCompartment;
    @XmlElement(namespace = "http://bpmndi.org")
    protected List<SequenceFlowConnector> sequenceFlowConnector;
//    @XmlElementRef(name = "associationConnector", namespace = "http://bpmndi.org", type = JAXBElement.class)
//    protected List<JAXBElement<AssociationConnectorType1>> associationConnector;
//    @XmlElement(namespace = "http://bpmndi.org")
//    protected List<DataAssociationConnector> dataAssociationConnector;
    @XmlAttribute
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object processRef;

    /**
     * Gets the value of the laneCompartment property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneCompartment property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLaneCompartment().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LaneCompartmentType1 }
     * 
     * 
     */
    public List<LaneCompartment> getLaneCompartment() {
        if (laneCompartment == null) {
            laneCompartment = new ArrayList<LaneCompartment>();
        }
        return this.laneCompartment;
    }

    /**
     * Gets the value of the sequenceFlowConnector property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sequenceFlowConnector property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSequenceFlowConnector().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SequenceFlowConnector }
     * 
     * 
     */
    public List<SequenceFlowConnector> getSequenceFlowConnector() {
        if (sequenceFlowConnector == null) {
            sequenceFlowConnector = new ArrayList<SequenceFlowConnector>();
        }
        return this.sequenceFlowConnector;
    }

    /**
     * Gets the value of the associationConnector property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the associationConnector property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssociationConnector().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AssociationConnectorType1 }{@code >}
     * {@link JAXBElement }{@code <}{@link AssociationConnectorType1 }{@code >}
     * 
     * 
     */
//    public List<JAXBElement<AssociationConnectorType1>> getAssociationConnector() {
//        if (associationConnector == null) {
//            associationConnector = new ArrayList<JAXBElement<AssociationConnectorType1>>();
//        }
//        return this.associationConnector;
//    }

    /**
     * Gets the value of the dataAssociationConnector property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataAssociationConnector property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataAssociationConnector().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataAssociationConnector }
     * 
     * 
     */
//    public List<DataAssociationConnector> getDataAssociationConnector() {
//        if (dataAssociationConnector == null) {
//            dataAssociationConnector = new ArrayList<DataAssociationConnector>();
//        }
//        return this.dataAssociationConnector;
//    }

    /**
     * Gets the value of the processRef property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getProcessRef() {
        return processRef;
    }

    /**
     * Sets the value of the processRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setProcessRef(Object value) {
        this.processRef = value;
    }

}
