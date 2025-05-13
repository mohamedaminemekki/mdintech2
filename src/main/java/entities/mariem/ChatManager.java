package entities.mariem;

import java.util.*;
import java.util.regex.Pattern;

public class ChatManager {
    private List<Ville> villes;
    private Ville currentVille;
    private String currentScenario = "";
    private int storyStep = 0;

    // Patterns pour détecter les intentions
    private static final Pattern GREETING_PATTERN = Pattern.compile(".*\\b(bonjour|salut|hello|coucou|hi|hey)\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern CITY_REQUEST_PATTERN = Pattern.compile(".*\\b(ville|endroit|lieu|visiter|aller|séjour|vacances)\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern INTERACTIVE_STORY_PATTERN = Pattern.compile(".*\\b(aventure interactive|histoire interactive|scénario|jeu)\\b.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern HELP_PATTERN = Pattern.compile(".*\\b(aide|help)\\b.*", Pattern.CASE_INSENSITIVE);

    // Mots-clés associés aux intentions
    private static final Map<String, List<String>> INTENT_KEYWORDS = new HashMap<>();
    static {
        INTENT_KEYWORDS.put("histoire", Arrays.asList("histoire", "historique", "passé", "origine"));
        INTENT_KEYWORDS.put("gastronomie", Arrays.asList("manger", "nourriture", "plat", "restaurant", "cuisine", "spécialité"));
        INTENT_KEYWORDS.put("nature", Arrays.asList("nature", "parc", "plage", "jardin", "montagne", "rivière"));
        INTENT_KEYWORDS.put("activités", Arrays.asList("activités", "choses à faire", "visiter", "excursion", "sortie"));
        INTENT_KEYWORDS.put("anecdotes", Arrays.asList("anecdote", "curiosité", "fait intéressant", "insolite"));
    }

    public ChatManager(List<Ville> villes) {
        this.villes = villes;
    }

    /**
     * Réinitialise la conversation et l'état du ChatManager.
     */
    public void resetConversation() {
        currentVille = null;
        currentScenario = "";
        storyStep = 0;
    }

    /**
     * Retourne la liste des noms de villes disponibles sous forme de chaîne.
     */
    public String getAvailableCityNames() {
        StringBuilder sb = new StringBuilder();
        for (Ville ville : villes) {
            sb.append(ville.getNom()).append(" ");
        }
        return sb.toString().trim();
    }

    public ChatResponse processMessage(String message) {
        message = message.toLowerCase().trim();
        ChatResponse response = new ChatResponse();

        // Commande d'aide
        if (HELP_PATTERN.matcher(message).matches()) {
            response.setMessage(generateHelpMessage());
            response.setOptions(Arrays.asList("histoire", "activités", "gastronomie", "nature", "anecdotes", "aventure interactive"));
            return response;
        }

        // Si aucune ville n'est sélectionnée, tenter d'extraire une ville depuis le message
        if (currentVille == null) {
            Ville extracted = extractCity(message);
            if (extracted != null) {
                currentVille = extracted;
                // Si le message est uniquement le nom de la ville, intégrer un greeting personnalisé
                response.setMessage("Vous voulez savoir sur " + currentVille.getNom() + ". " + generateGreeting());
                response.setOptions(getAvailableTopics());
                return response;
            } else {
                response.setMessage("Pour quelle ville souhaitez-vous des informations ?");
                response.setOptions(Arrays.asList(getAvailableCityNames()));
                return response;
            }
        }

        // Si le message correspond à un greeting
        if (GREETING_PATTERN.matcher(message).matches()) {
            response.setMessage(generateGreeting());
            response.setOptions(getAvailableTopics());
            return response;
        }
        // Si le message demande à changer ou redemander des infos sur la ville
        if (CITY_REQUEST_PATTERN.matcher(message).matches()) {
            response.setMessage("Vous avez sélectionné " + currentVille.getNom() + ". Que souhaitez-vous savoir ?");
            response.setOptions(getAvailableTopics());
            return response;
        }
        // Si le message lance une aventure interactive
        if (INTERACTIVE_STORY_PATTERN.matcher(message).matches()) {
            if (currentVille.getHistoireInteractive() == null || currentVille.getHistoireInteractive().isEmpty()) {
                response.setMessage("Désolé, aucune aventure interactive n'est disponible pour " + currentVille.getNom() + ".");
            } else {
                currentScenario = "AVENTURE";
                storyStep = 0;
                response.setMessage(demarrerAventure());
            }
            return response;
        }

        // Détection d'une intention spécifique par mots-clés
        String intent = detectIntent(message);
        if (!intent.equals("unknown")) {
            handleSpecificRequest(intent, response);
        } else {
            response.setMessage(generateFallbackResponse());
            response.setOptions(getAvailableTopics());
        }
        return response;
    }

    /**
     * Extrait la ville à partir du message en comparant avec les noms des villes disponibles.
     */
    private Ville extractCity(String message) {
        for (Ville ville : villes) {
            if (message.contains(ville.getNom().toLowerCase())) {
                return ville;
            }
        }
        return null;
    }

    /**
     * Retourne la liste des sujets disponibles pour la ville sélectionnée.
     */
    private List<String> getAvailableTopics() {
        return Arrays.asList("histoire", "activités", "gastronomie", "nature", "anecdotes", "aventure interactive");
    }

    /**
     * Détecte l'intention spécifique à partir du message en recherchant des mots-clés.
     */
    private String detectIntent(String message) {
        for (Map.Entry<String, List<String>> entry : INTENT_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (message.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return "unknown";
    }

    /**
     * Gère la requête spécifique en fonction de l'intention détectée.
     */
    private void handleSpecificRequest(String intent, ChatResponse response) {
        switch (intent) {
            case "histoire":
                response.setMessage(formatResponse("Histoire", currentVille.getHistoire()));
                break;
            case "gastronomie":
                response.setMessage(formatResponse("Gastronomie", currentVille.getGastronomie()));
                break;
            case "nature":
                response.setMessage(formatResponse("Nature", currentVille.getNature()));
                break;
            case "activités":
                response.setMessage(formatResponse("Activités", currentVille.getActivites()));
                break;
            case "anecdotes":
                response.setMessage(formatResponse("Anecdotes", currentVille.getAnecdotes()));
                break;
            default:
                response.setMessage(generateFallbackResponse());
                response.setOptions(getAvailableTopics());
                break;
        }
    }

    /**
     * Formate la réponse en ajoutant un titre et une invitation à poursuivre.
     */
    private String formatResponse(String title, String content) {
        return "**" + title + "**\n"
                + (content != null && !content.isEmpty() ? content : "Aucune information disponible")
                + "\n\nQue souhaitez-vous savoir d'autre ?";
    }

    /**
     * Génère un message d'accueil personnalisé incluant le nom de la ville.
     */
    private String generateGreeting() {
        return "Bonjour ! Je suis Mariem, votre guide virtuel pour " + currentVille.getNom() + ".\n"
                + "Posez-moi vos questions ou choisissez parmi les thèmes suivants.";
    }

    /**
     * Génère un message d'aide listant les commandes disponibles.
     */
    private String generateHelpMessage() {
        return "Commandes disponibles :\n"
                + "- Indiquez le nom d'une ville pour obtenir des informations.\n"
                + "- Demandez des détails en tapant 'histoire', 'activités', 'gastronomie', 'nature', ou 'anecdotes'.\n"
                + "- Tapez 'aventure interactive' pour lancer une histoire interactive.\n"
                + "- Tapez 'aide' pour afficher ce message.";
    }

    /**
     * Message de repli lorsque la demande n'est pas comprise.
     */
    private String generateFallbackResponse() {
        return "Je n'ai pas bien compris votre demande. Essayez de demander par exemple : 'histoire', 'gastronomie', etc.";
    }

    /**
     * Gère le démarrage et la progression d'une aventure interactive.
     * Ici, l'histoire interactive est supposée être un texte avec plusieurs étapes séparées par '||'.
     */
    private String demarrerAventure() {
        String adventureStory = currentVille.getHistoireInteractive();
        String[] steps = adventureStory.split("\\|\\|");
        if (storyStep < steps.length) {
            String currentStep = steps[storyStep];
            storyStep++;
            return currentStep + "\n\nTapez 'suivant' pour continuer.";
        } else {
            currentScenario = "";
            storyStep = 0;
            return "L'aventure est terminée. Que souhaitez-vous explorer d'autre ?";
        }
    }

    /**
     * Classe interne pour encapsuler la réponse du chatbot.
     */
    public static class ChatResponse {
        private String message;
        private List<String> options = new ArrayList<>();

        public String getMessage() {
            return message;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }
    }
}
