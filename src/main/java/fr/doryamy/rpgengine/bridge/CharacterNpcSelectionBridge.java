package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.util.RpgLogger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/** Bridge NeoForge dédié à la sélection directe d'un PNJ Citizens pour un personnage. */
public final class CharacterNpcSelectionBridge {
    private Method showSelectionResultMethod;
    private Method clearSelectionRequestHandlerMethod;
    private Consumer<UUID> selectionRequestHandler;

    public void initialize(Class<?> bridgeClass) throws ReflectiveOperationException {
        Objects.requireNonNull(bridgeClass,"bridgeClass");
        Method register = bridgeClass.getMethod("registerCharacterNpcSelectionRequestHandler", Consumer.class);
        clearSelectionRequestHandlerMethod = bridgeClass.getMethod("clearCharacterNpcSelectionRequestHandler");
        showSelectionResultMethod = bridgeClass.getMethod("showCharacterNpcSelectionResult", UUID.class, String.class, String.class);
        register.invoke(null, (Consumer<UUID>) this::handleSelectionRequest);
    }
    public void setSelectionRequestHandler(Consumer<UUID> handler) { selectionRequestHandler=Objects.requireNonNull(handler,"handler"); }
    public boolean showSelectionResult(UUID playerUuid,String npcId,String npcName) {
        return invokeBoolean(showSelectionResultMethod,"résultat de sélection Citizens",playerUuid,npcId,npcName);
    }
    private boolean invokeBoolean(Method method,String operation,Object... args) {
        if(method==null){ RpgLogger.error("Le bridge de sélection Citizens n'est pas initialisé."); return false; }
        try { Object result=method.invoke(null,args); return result instanceof Boolean success && success; }
        catch(IllegalAccessException|InvocationTargetException e){ Throwable cause=e instanceof InvocationTargetException i?i.getCause():e; RpgLogger.error("Impossible d'envoyer "+operation+" : "+(cause!=null?cause.getMessage():e.getMessage())); return false; }
    }
    private void handleSelectionRequest(UUID playerUuid) {
        Consumer<UUID> handler=selectionRequestHandler;
        if(handler==null){ RpgLogger.error("REQUEST_CHARACTER_NPC_SELECTION reçu mais aucun contrôleur n'est disponible."); return; }
        try { handler.accept(playerUuid); } catch(RuntimeException e){ RpgLogger.error("REQUEST_CHARACTER_NPC_SELECTION refusé pour "+playerUuid+" : "+e.getMessage()); }
    }
    public void shutdown(){
        if(clearSelectionRequestHandlerMethod!=null) try{clearSelectionRequestHandlerMethod.invoke(null);}catch(ReflectiveOperationException e){RpgLogger.error("Impossible de supprimer le handler REQUEST_CHARACTER_NPC_SELECTION : "+e.getMessage());}
        selectionRequestHandler=null; showSelectionResultMethod=null; clearSelectionRequestHandlerMethod=null;
    }
}
