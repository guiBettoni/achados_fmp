package com.example.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.R;
import com.example.model.ItemModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemRepository {
    private static final String PREF_NAME = "fmp_items_repo_v4";
    private static final String KEY_ITEMS_JSON = "items_data_v4";
    private static ItemRepository instance;

    private final SharedPreferences pref;
    private final List<ItemModel> cachedItems = new ArrayList<>();

    private ItemRepository(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadItems();
    }

    public static synchronized ItemRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ItemRepository(context);
        }
        return instance;
    }

    private void loadItems() {
        cachedItems.clear();
        String jsonString = pref.getString(KEY_ITEMS_JSON, null);
        if (jsonString != null && !jsonString.trim().isEmpty()) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    ItemModel item = new ItemModel(
                            obj.getString("id"),
                            obj.getString("title"),
                            obj.getString("category"),
                            obj.getString("type"),
                            obj.getString("date"),
                            obj.getString("location"),
                            obj.getString("description"),
                            obj.optString("observations", ""),
                            obj.getString("status"),
                            obj.optString("registeredByEmail", ""),
                            obj.optString("registeredByName", ""),
                            obj.optString("holdingLocation", "Secretaria"),
                            obj.optInt("imageRes", 0),
                            obj.optLong("timestamp", System.currentTimeMillis()),
                            obj.optString("photoPath", null),
                            obj.optString("protocolCode", null)
                    );
                    cachedItems.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                seedInitialData();
            }
        } else {
            seedInitialData();
        }
    }

    private void saveItems() {
        JSONArray array = new JSONArray();
        for (ItemModel item : cachedItems) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", item.getId());
                obj.put("title", item.getTitle());
                obj.put("category", item.getCategory());
                obj.put("type", item.getType());
                obj.put("date", item.getDate());
                obj.put("location", item.getLocation());
                obj.put("description", item.getDescription());
                obj.put("observations", item.getObservations());
                obj.put("status", item.getStatus());
                obj.put("registeredByEmail", item.getRegisteredByEmail());
                obj.put("registeredByName", item.getRegisteredByName());
                obj.put("holdingLocation", item.getHoldingLocation());
                obj.put("imageRes", item.getImageRes());
                obj.put("timestamp", item.getTimestamp());
                obj.put("photoPath", item.getPhotoPath() != null ? item.getPhotoPath() : "");
                obj.put("protocolCode", item.getDisplayProtocolCode());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        pref.edit().putString(KEY_ITEMS_JSON, array.toString()).apply();
    }

    private void seedInitialData() {
        cachedItems.clear();
        long now = System.currentTimeMillis();

        // 1. Fone de ouvido (Mockup Tela 3 & 4)
        cachedItems.add(new ItemModel(
                "fmp-001",
                "Fone de ouvido",
                "Eletrônicos",
                "FOUND",
                "11/09/2026",
                "Laboratório 4",
                "Fone de ouvido Sony preto intra-auricular com estojo carregador.",
                "Encontrado próximo às mesas centrais do laboratório 4.",
                "WAITING_PICKUP",
                "guilherme@" + SessionManager.EMAIL_DOMAIN,
                "Guilherme Bettoni",
                "Secretaria",
                R.drawable.thumb_earbuds,
                now - 3600000L * 4
        ));

        // 2. Chave (Mockup Tela 3 & 4)
        cachedItems.add(new ItemModel(
                "fmp-002",
                "Chave",
                "Chaves",
                "FOUND",
                "10/09/2026",
                "Sala 08",
                "Molho com 4 chaves e chaveiro de madeira gravado 'HOME 204'.",
                "Entregue na secretaria acadêmica após aula do Bloco A.",
                "DELIVERED",
                "secretaria@" + SessionManager.EMAIL_DOMAIN,
                "Secretaria Acadêmica",
                "Secretaria",
                R.drawable.thumb_keys,
                now - 3600000L * 24
        ));

        // 3. Carteira (Mockup Tela 3 & 4)
        cachedItems.add(new ItemModel(
                "fmp-003",
                "Carteira",
                "Documentos",
                "FOUND",
                "08/09/2026",
                "Auditório",
                "Carteira masculina de couro marrom com cartões e documentos.",
                "Encontrada na última fileira de poltronas do auditório principal.",
                "WAITING_PICKUP",
                "guilherme@" + SessionManager.EMAIL_DOMAIN,
                "Guilherme Bettoni",
                "Secretaria",
                R.drawable.thumb_wallet,
                now - 3600000L * 48
        ));

        // 4. Garrafa térmica (Mockup Tela 3 & 4)
        cachedItems.add(new ItemModel(
                "fmp-004",
                "Garrafa térmica",
                "Outros",
                "FOUND",
                "08/09/2026",
                "Biblioteca",
                "Garrafa térmica preta 18oz de aço inox com tampa e alça.",
                "Esquecida nas baias individuais de estudo da Biblioteca Central.",
                "WAITING_PICKUP",
                "apoio@" + SessionManager.EMAIL_DOMAIN,
                "Equipe de Apoio FMP",
                "Secretaria",
                R.drawable.thumb_bottle,
                now - 3600000L * 52
        ));

        // 5. Caderno de Direito (Objeto Perdido pelo aluno - Mockup Tela 11)
        cachedItems.add(new ItemModel(
                "fmp-005",
                "Caderno Universitário 10 Matérias",
                "Livros",
                "LOST",
                "05/09/2026",
                "Sala 12",
                "Caderno espiral capa dura com anotações de Direito Constitucional.",
                "Esquecido sob a carteira da primeira fileira.",
                "SEARCHING",
                "guilherme@" + SessionManager.EMAIL_DOMAIN,
                "Guilherme Bettoni",
                "Secretaria",
                0,
                now - 3600000L * 90
        ));

        // 6. Casaco Moletom
        cachedItems.add(new ItemModel(
                "fmp-006",
                "Casaco Moletom Cinza",
                "Roupas",
                "FOUND",
                "03/09/2026",
                "Quadra Poliesportiva",
                "Agasalho esportivo tamanho M com capuz.",
                "Deixado na arquibancada da quadra durante o intervalo.",
                "WAITING_PICKUP",
                "esportes@" + SessionManager.EMAIL_DOMAIN,
                "Coordenação de Esportes",
                "Secretaria",
                0,
                now - 3600000L * 120
        ));

        // 7. Calculadora Científica (Objeto Perdido)
        cachedItems.add(new ItemModel(
                "fmp-007",
                "Calculadora Científica Casio",
                "Eletrônicos",
                "LOST",
                "02/09/2026",
                "Laboratório 2",
                "Calculadora Casio fx-82MS com tampa protetora cinza.",
                "Esquecida na bancada 08 após aula de estatística.",
                "SEARCHING",
                "guilherme@" + SessionManager.EMAIL_DOMAIN,
                "Guilherme Bettoni",
                "Secretaria",
                0,
                now - 3600000L * 150
        ));

        saveItems();
    }

    public synchronized List<ItemModel> getAllItems() {
        List<ItemModel> list = new ArrayList<>(cachedItems);
        Collections.sort(list, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
        return list;
    }

    public synchronized List<ItemModel> getRecentFoundItems() {
        List<ItemModel> result = new ArrayList<>();
        List<ItemModel> sorted = getAllItems();
        for (ItemModel item : sorted) {
            if ("FOUND".equalsIgnoreCase(item.getType())) {
                result.add(item);
                if (result.size() >= 4) break;
            }
        }
        return result;
    }

    public synchronized List<ItemModel> getMyItems(String userEmail, String type) {
        List<ItemModel> result = new ArrayList<>();
        for (ItemModel item : getAllItems()) {
            boolean typeMatch = type == null || type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase(item.getType());
            // If email matches registered user
            boolean emailMatch = userEmail != null && userEmail.equalsIgnoreCase(item.getRegisteredByEmail());
            if (emailMatch && typeMatch) {
                result.add(item);
            }
        }
        return result;
    }

    public synchronized List<ItemModel> searchAndFilter(String query, String category, String status, String type) {
        List<ItemModel> result = new ArrayList<>();
        String q = query != null ? query.trim().toLowerCase() : "";

        for (ItemModel item : getAllItems()) {
            // Text search in title, category, location, description, observations
            boolean matchesQuery = q.isEmpty()
                    || item.getTitle().toLowerCase().contains(q)
                    || item.getDescription().toLowerCase().contains(q)
                    || item.getLocation().toLowerCase().contains(q)
                    || item.getCategory().toLowerCase().contains(q)
                    || item.getObservations().toLowerCase().contains(q);

            // Flexible category match
            boolean matchesCategory;
            if (category == null || category.equalsIgnoreCase("Todos") || category.equalsIgnoreCase("Todas") || category.trim().isEmpty()) {
                matchesCategory = true;
            } else {
                String c1 = category.toLowerCase().trim();
                String c2 = item.getCategory().toLowerCase().trim();
                matchesCategory = c1.contains(c2) || c2.contains(c1);
            }

            // Flexible status match
            boolean matchesStatus;
            if (status == null || status.equalsIgnoreCase("Todos") || status.equalsIgnoreCase("Todas") || status.trim().isEmpty()) {
                matchesStatus = true;
            } else if ("WAITING_PICKUP".equalsIgnoreCase(status) || "Aguardando retirada".equalsIgnoreCase(status)) {
                matchesStatus = "WAITING_PICKUP".equalsIgnoreCase(item.getStatus());
            } else if ("DELIVERED".equalsIgnoreCase(status) || "Entregue".equalsIgnoreCase(status) || "Resolvido".equalsIgnoreCase(status)) {
                matchesStatus = "DELIVERED".equalsIgnoreCase(item.getStatus()) || "RESOLVED".equalsIgnoreCase(item.getStatus());
            } else if ("SEARCHING".equalsIgnoreCase(status) || "Perdido".equalsIgnoreCase(status)) {
                matchesStatus = "SEARCHING".equalsIgnoreCase(item.getStatus());
            } else {
                matchesStatus = item.getStatus().equalsIgnoreCase(status);
            }

            // Type match
            boolean matchesType;
            if (type == null || type.equalsIgnoreCase("ALL") || type.equalsIgnoreCase("Todos") || type.trim().isEmpty()) {
                matchesType = true;
            } else {
                matchesType = item.getType().equalsIgnoreCase(type);
            }

            if (matchesQuery && matchesCategory && matchesStatus && matchesType) {
                result.add(item);
            }
        }
        return result;
    }

    public synchronized ItemModel getItemById(String id) {
        if (id == null) return null;
        for (ItemModel item : cachedItems) {
            if (id.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }

    public synchronized void addItem(ItemModel item) {
        if (item.getId() == null || item.getId().isEmpty()) {
            item.setId("fmp-" + System.currentTimeMillis());
        }
        if (item.getTimestamp() == 0) {
            item.setTimestamp(System.currentTimeMillis());
        }
        cachedItems.add(0, item);
        saveItems();
    }

    public synchronized boolean updateItemStatus(String id, String newStatus) {
        for (ItemModel item : cachedItems) {
            if (item.getId().equals(id)) {
                item.setStatus(newStatus);
                saveItems();
                return true;
            }
        }
        return false;
    }

    public synchronized int getLostCount() {
        int count = 0;
        for (ItemModel item : cachedItems) {
            if ("LOST".equalsIgnoreCase(item.getType()) && "SEARCHING".equalsIgnoreCase(item.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public synchronized int getFoundCount() {
        int count = 0;
        for (ItemModel item : cachedItems) {
            if ("FOUND".equalsIgnoreCase(item.getType()) && !"DELIVERED".equalsIgnoreCase(item.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public synchronized int getActiveCount() {
        int count = 0;
        for (ItemModel item : cachedItems) {
            if ("WAITING_PICKUP".equals(item.getStatus()) || "SEARCHING".equals(item.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public synchronized int getDeliveredCount() {
        int count = 0;
        for (ItemModel item : cachedItems) {
            if ("DELIVERED".equals(item.getStatus()) || "RESOLVED".equals(item.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Retorna itens perdidos pelo usuário informado.
     */
    public synchronized List<ItemModel> getMyLostItems(String userEmail) {
        List<ItemModel> result = new ArrayList<>();
        for (ItemModel item : getAllItems()) {
            // Só perdas ainda abertas: um objeto já recuperado não precisa de correspondência.
            if ("LOST".equalsIgnoreCase(item.getType()) && "SEARCHING".equalsIgnoreCase(item.getStatus())) {
                boolean emailMatch = userEmail != null && userEmail.equalsIgnoreCase(item.getRegisteredByEmail());
                if (emailMatch) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    /**
     * Cruzamento Inteligente (Smart Match):
     * Localiza itens encontrados (FOUND) que coincidem com um item perdido (LOST)
     * por categoria, termos de busca e local aproximado.
     */
    public synchronized List<ItemModel> findMatchesForLostItem(ItemModel lostItem) {
        List<ItemModel> matches = new ArrayList<>();
        if (lostItem == null) return matches;

        String targetCategory = lostItem.getCategory() != null ? lostItem.getCategory().toLowerCase().trim() : "";
        String targetTitle = lostItem.getTitle() != null ? lostItem.getTitle().toLowerCase().trim() : "";
        String[] titleWords = targetTitle.split("\\s+");
        String targetLocation = lostItem.getLocation() != null ? lostItem.getLocation().toLowerCase().trim() : "";

        for (ItemModel item : cachedItems) {
            if (item.getId().equals(lostItem.getId())) continue;
            if (!"FOUND".equalsIgnoreCase(item.getType())) continue;
            if ("DELIVERED".equalsIgnoreCase(item.getStatus())) continue;

            int score = 0;
            // 1. Categoria idêntica ou contida
            if (!targetCategory.isEmpty() && item.getCategory() != null) {
                String cat = item.getCategory().toLowerCase().trim();
                if (cat.contains(targetCategory) || targetCategory.contains(cat)) {
                    score += 3;
                }
            }

            // 2. Palavras no título (ex.: fone, chave, carteira, caderno, casaco)
            String otherTitle = item.getTitle() != null ? item.getTitle().toLowerCase() : "";
            for (String word : titleWords) {
                if (word.length() >= 3 && otherTitle.contains(word)) {
                    score += 3;
                }
            }

            // 3. Proximidade de Local
            String otherLocation = item.getLocation() != null ? item.getLocation().toLowerCase() : "";
            if (!targetLocation.isEmpty() && !otherLocation.isEmpty()) {
                if (targetLocation.contains(otherLocation) || otherLocation.contains(targetLocation)) {
                    score += 1;
                }
            }

            if (score >= 3) {
                matches.add(item);
            }
        }
        return matches;
    }

    /**
     * Retorna o principal Smart Match ativo para o usuário atual (objeto encontrado correspondente).
     */
    public synchronized ItemModel getTopSmartMatchForUser(String userEmail) {
        List<ItemModel> myLost = getMyLostItems(userEmail);
        for (ItemModel lost : myLost) {
            List<ItemModel> matches = findMatchesForLostItem(lost);
            if (!matches.isEmpty()) {
                return matches.get(0);
            }
        }
        return null;
    }

    /**
     * Retorna o item perdido que gerou correspondência com o item encontrado indicado.
     */
    public synchronized ItemModel getMatchedLostItem(ItemModel foundItem, String userEmail) {
        if (foundItem == null) return null;
        List<ItemModel> myLost = getMyLostItems(userEmail);
        for (ItemModel lost : myLost) {
            List<ItemModel> matches = findMatchesForLostItem(lost);
            for (ItemModel m : matches) {
                if (m.getId().equals(foundItem.getId())) {
                    return lost;
                }
            }
        }
        return null;
    }
}
