package org.tmf.dsmapi.troubleTicket.hub.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
//import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.tmf.dsmapi.commons.exceptions.BadUsageException;
import org.tmf.dsmapi.troubleTicket.model.TroubleTicket;
import org.tmf.dsmapi.troubleTicket.hub.model.TroubleTicketEvent;
import org.tmf.dsmapi.troubleTicket.hub.model.TroubleTicketEventTypeEnum;
import org.tmf.dsmapi.troubleTicket.hub.service.TroubleTicketRESTEventPublisherLocal;
import org.tmf.dsmapi.hub.model.Hub;
import org.tmf.dsmapi.hub.service.HubFacade;

/**
 *
 * @author pierregauthier should be async or called with MDB
 */
@Stateless
//@Asynchronous
public class TroubleTicketEventPublisher implements TroubleTicketEventPublisherLocal {

    @EJB
    HubFacade hubFacade;
    @EJB
    TroubleTicketEventFacade eventFacade;
    @EJB
    TroubleTicketRESTEventPublisherLocal restEventPublisherLocal;

    /** 
     * Add business logic below. (Right-click in editor and choose
     * "Insert Code > Add Business Method")
     * Access Hubs using callbacks and send to http publisher 
     *(pool should be configured around the RESTEventPublisher bean)
     * Loop into array of Hubs
     * Call RestEventPublisher - Need to implement resend policy plus eviction
     * Filtering is done in RestEventPublisher based on query expression
    */ 
    @Override
    @Asynchronous
    public synchronized void publish(TroubleTicketEvent event) {
        try {
            eventFacade.create(event);
        } catch (BadUsageException ex) {
            Logger.getLogger(TroubleTicketEventPublisher.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<Hub> hubList = hubFacade.findAll();
        Iterator<Hub> it = hubList.iterator();
        while (it.hasNext()) {
            Hub hub = it.next();
            restEventPublisherLocal.publish(hub, event);
        }
    }

    @Override
    public void createNotification(TroubleTicket bean, String reason, Date date) {
        TroubleTicketEvent event = new TroubleTicketEvent();
        event.setResource(bean);
        event.setEventTime(date);
       
        event.setEventType(TroubleTicketEventTypeEnum.TroubleTicketCreationNotification);
        publish(event);

    }

    @Override
    public void deletionNotification(TroubleTicket bean, String reason, Date date) {
        TroubleTicketEvent event = new TroubleTicketEvent();
        event.setResource(bean);
        event.setEventTime(date);
       
        event.setEventType(TroubleTicketEventTypeEnum.TroubleTicketDeletionNotification);
        publish(event);
    }
	
    @Override
    public void updateNotification(TroubleTicket bean, String reason, Date date) {
        TroubleTicketEvent event = new TroubleTicketEvent();
        event.setResource(bean);
        event.setEventTime(date);
        
        event.setEventType(TroubleTicketEventTypeEnum.TroubleTicketUpdateNotification);
        publish(event);
    }

    @Override
    public void valueChangedNotification(TroubleTicket bean, String reason, Date date) {
        TroubleTicketEvent event = new TroubleTicketEvent();
        event.setResource(bean);
        event.setEventTime(date);
        event.setEventType(TroubleTicketEventTypeEnum.TroubleTicketValueChangeNotification);
        publish(event);
    }
}