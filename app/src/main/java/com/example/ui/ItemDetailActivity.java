package com.example.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.R;
import com.example.data.ItemRepository;
import com.example.data.SessionManager;
import com.example.model.ItemModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.List;

public class ItemDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private ItemRepository repository;
    private SessionManager sessionManager;
    private ItemModel currentItem;

    private ImageView ivDetailPhoto;
    private TextView tvDetailCategoryPill;
    private TextView tvDetailTitle;
    private TextView tvDetailDate;
    private TextView tvDetailLocation;
    private View layoutDetailHoldingLocation;
    private TextView tvDetailHoldingLocation;
    private ImageView ivDetailStatusIcon;
    private TextView tvDetailStatus;
    private TextView tvDetailDescription;
    private TextView tvDetailObservations;

    // Smart Match
    private View cardDetailSmartMatch;
    private TextView tvDetailSmartMatchText;
    private MaterialButton btnOpenDetailMatch;

    // Registration Code (B1)
    private View cardProtocolCode;
    private TextView tvProtocolCode;
    private View btnCopyProtocol;

    // Timeline
    private TextView tvTimelineStep1Date;
    private View layoutTimelineStep2;
    private ImageView ivTimelineStep2Icon;
    private View vTimelineStep2Line;
    private TextView tvTimelineStep2Title;
    private TextView tvTimelineStep2Sub;
    private View layoutTimelineStep3;
    private ImageView ivTimelineStep3Icon;
    private View layoutTimelineStep4;
    private ImageView ivTimelineStep4Icon;
    private TextView tvTimelineStep4Title;
    private TextView tvTimelineStep4Sub;

    // Action buttons
    private MaterialButton btnHowToClaim;
    private MaterialButton btnToggleDelivered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        repository = ItemRepository.getInstance(this);
        sessionManager = new SessionManager(this);

        String itemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        if (itemId != null) {
            currentItem = repository.getItemById(itemId);
        }

        initViews();
        bindData();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.detailToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ivDetailPhoto = findViewById(R.id.ivDetailPhoto);
        tvDetailCategoryPill = findViewById(R.id.tvDetailCategoryPill);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        layoutDetailHoldingLocation = findViewById(R.id.layoutDetailHoldingLocation);
        tvDetailHoldingLocation = findViewById(R.id.tvDetailHoldingLocation);
        ivDetailStatusIcon = findViewById(R.id.ivDetailStatusIcon);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailObservations = findViewById(R.id.tvDetailObservations);

        // Smart Match
        cardDetailSmartMatch = findViewById(R.id.cardDetailSmartMatch);
        tvDetailSmartMatchText = findViewById(R.id.tvDetailSmartMatchText);
        btnOpenDetailMatch = findViewById(R.id.btnOpenDetailMatch);

        // Registration Code (B1)
        cardProtocolCode = findViewById(R.id.cardProtocolCode);
        tvProtocolCode = findViewById(R.id.tvProtocolCode);
        btnCopyProtocol = findViewById(R.id.btnCopyProtocol);

        // Timeline
        tvTimelineStep1Date = findViewById(R.id.tvTimelineStep1Date);
        layoutTimelineStep2 = findViewById(R.id.layoutTimelineStep2);
        ivTimelineStep2Icon = findViewById(R.id.ivTimelineStep2Icon);
        vTimelineStep2Line = findViewById(R.id.vTimelineStep2Line);
        tvTimelineStep2Title = findViewById(R.id.tvTimelineStep2Title);
        tvTimelineStep2Sub = findViewById(R.id.tvTimelineStep2Sub);
        layoutTimelineStep3 = findViewById(R.id.layoutTimelineStep3);
        ivTimelineStep3Icon = findViewById(R.id.ivTimelineStep3Icon);
        layoutTimelineStep4 = findViewById(R.id.layoutTimelineStep4);
        ivTimelineStep4Icon = findViewById(R.id.ivTimelineStep4Icon);
        tvTimelineStep4Title = findViewById(R.id.tvTimelineStep4Title);
        tvTimelineStep4Sub = findViewById(R.id.tvTimelineStep4Sub);

        // Actions
        btnHowToClaim = findViewById(R.id.btnHowToClaim);
        btnToggleDelivered = findViewById(R.id.btnToggleDelivered);

        btnHowToClaim.setOnClickListener(v -> showPickupInstructionsDialog());

        btnCopyProtocol.setOnClickListener(v -> copyProtocolToClipboard());

        btnToggleDelivered.setOnClickListener(v -> {
            if (currentItem != null) {
                boolean isDelivered = "DELIVERED".equalsIgnoreCase(currentItem.getStatus())
                        || "RESOLVED".equalsIgnoreCase(currentItem.getStatus());
                boolean isLost = "LOST".equalsIgnoreCase(currentItem.getType());

                String confirmMessage;
                if (isDelivered) {
                    confirmMessage = isLost
                            ? "Reabrir esta ocorrência de perda? O objeto voltará a aparecer como procurado."
                            : "Reabrir esta ocorrência? O objeto voltará a aparecer como aguardando o dono.";
                } else {
                    confirmMessage = isLost
                            ? "Marcar este objeto como recuperado? Ele deixará de aparecer como procurado no campus."
                            : "Marcar este objeto como devolvido? Ele deixará de aparecer como disponível para o campus.";
                }

                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Confirmar alteração")
                        .setMessage(confirmMessage)
                        .setPositiveButton("Confirmar", (dialog, which) -> {
                            String newStatus;
                            if (isLost) {
                                newStatus = isDelivered ? "SEARCHING" : "RESOLVED";
                            } else {
                                newStatus = isDelivered ? "WAITING_PICKUP" : "DELIVERED";
                            }
                            repository.updateItemStatus(currentItem.getId(), newStatus);
                            currentItem.setStatus(newStatus);
                            bindData();
                            String msg = isDelivered
                                    ? "Ocorrência reaberta com sucesso."
                                    : (isLost ? "Objeto marcado como recuperado!" : "Objeto marcado como devolvido ao dono!");
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
    }

    private void bindData() {
        if (currentItem == null) {
            tvDetailTitle.setText("Item não encontrado");
            return;
        }

        tvDetailTitle.setText(currentItem.getTitle());
        tvDetailCategoryPill.setText(currentItem.getCategory());
        tvDetailDate.setText(currentItem.getDate());
        tvDetailLocation.setText(currentItem.getLocation());
        tvDetailHoldingLocation.setText(currentItem.getHoldingLocation() != null ? currentItem.getHoldingLocation() : "Secretaria");
        tvDetailDescription.setText(currentItem.getDescription());
        tvDetailObservations.setText(currentItem.getObservations());

        // Hide holding location for LOST items (A3)
        boolean isLost = "LOST".equalsIgnoreCase(currentItem.getType());
        if (layoutDetailHoldingLocation != null) {
            layoutDetailHoldingLocation.setVisibility(isLost ? View.GONE : View.VISIBLE);
        }

        // Visibilidade de btnToggleDelivered (Regra 1):
        // • item FOUND: visível somente para a Secretaria;
        // • item LOST: visível para quem cadastrou o item e para a Secretaria;
        // • nos demais casos: View.GONE.
        String currentUserEmail = sessionManager.getUserEmail();
        boolean isSecretaria = sessionManager.isSecretaria();
        boolean isOwner = currentUserEmail != null && currentItem.getRegisteredByEmail() != null
                && currentUserEmail.equalsIgnoreCase(currentItem.getRegisteredByEmail());

        if ("FOUND".equalsIgnoreCase(currentItem.getType())) {
            btnToggleDelivered.setVisibility(isSecretaria ? View.VISIBLE : View.GONE);
        } else if ("LOST".equalsIgnoreCase(currentItem.getType())) {
            btnToggleDelivered.setVisibility((isOwner || isSecretaria) ? View.VISIBLE : View.GONE);
        } else {
            btnToggleDelivered.setVisibility(View.GONE);
        }

        // Thumbnail photo (custom uploaded vs seeded vs fallback)
        if (currentItem.getPhotoPath() != null && !currentItem.getPhotoPath().isEmpty() && new File(currentItem.getPhotoPath()).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentItem.getPhotoPath());
            if (bitmap != null) {
                ivDetailPhoto.setImageBitmap(bitmap);
                ivDetailPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivDetailPhoto.setPadding(0, 0, 0, 0);
            } else {
                setFallbackIcon();
            }
        } else if (currentItem.getImageRes() != 0) {
            ivDetailPhoto.setImageResource(currentItem.getImageRes());
            ivDetailPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivDetailPhoto.setPadding(0, 0, 0, 0);
        } else {
            setFallbackIcon();
        }

        // Registration Code (B1: esconda todo o bloco do código para itens LOST)
        String protocol = currentItem.getDisplayProtocolCode();
        tvProtocolCode.setText(protocol);
        if (cardProtocolCode != null) {
            cardProtocolCode.setVisibility(isLost ? View.GONE : View.VISIBLE);
        }

        // Status presentation & Timeline
        setupStatusAndTimeline();

        // Smart Match check
        setupSmartMatch();
    }

    private void setFallbackIcon() {
        ivDetailPhoto.setImageResource(getCategoryFallbackIcon(currentItem.getCategory()));
        ivDetailPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = (int) (48 * getResources().getDisplayMetrics().density);
        ivDetailPhoto.setPadding(pad, pad, pad, pad);
    }

    private void setupStatusAndTimeline() {
        String status = currentItem.getStatus();
        String type = currentItem.getType();
        boolean isDelivered = "DELIVERED".equalsIgnoreCase(status) || "RESOLVED".equalsIgnoreCase(status);
        boolean isWaitingPickup = "WAITING_PICKUP".equalsIgnoreCase(status);
        boolean isLost = "LOST".equalsIgnoreCase(type);

        tvTimelineStep1Date.setText("Cadastrado em " + currentItem.getDate());

        // A ordem importa: primeiro verificamos se a ocorrência já foi resolvida.
        // Um item perdido continua sendo do tipo LOST para sempre, então se
        // perguntássemos "é perdido?" antes, o caso "resolvido" nunca seria alcançado.
        if (isLost && isDelivered) {
            tvDetailStatus.setText("Devolvido");
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.fluent_status_delivered_text));
            ivDetailStatusIcon.setImageResource(R.drawable.ic_check);
            ivDetailStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_status_delivered_text));
            btnToggleDelivered.setText("Reabrir ocorrência");

            if (layoutTimelineStep2 != null) layoutTimelineStep2.setVisibility(View.VISIBLE);
            if (tvTimelineStep2Title != null) tvTimelineStep2Title.setText("2. Objeto recuperado pelo dono");
            if (tvTimelineStep2Sub != null) tvTimelineStep2Sub.setText("Ocorrência encerrada");
            if (vTimelineStep2Line != null) vTimelineStep2Line.setVisibility(View.GONE);
            if (ivTimelineStep2Icon != null) {
                ivTimelineStep2Icon.setImageResource(R.drawable.ic_check);
                ivTimelineStep2Icon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_status_delivered_text));
            }
            if (layoutTimelineStep3 != null) layoutTimelineStep3.setVisibility(View.GONE);
            if (layoutTimelineStep4 != null) layoutTimelineStep4.setVisibility(View.GONE);

        } else if (isLost) {
            tvDetailStatus.setText("Procura-se");
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.fluent_status_lost_text));
            ivDetailStatusIcon.setImageResource(R.drawable.ic_clock);
            ivDetailStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_status_lost_text));
            btnToggleDelivered.setText("Marcar como recuperado");

            // A4: Para itens LOST, mostrar apenas etapas "Ocorrência cadastrada" e "Aguardando alguém encontrar o objeto", sem etapas de retirada
            if (layoutTimelineStep2 != null) layoutTimelineStep2.setVisibility(View.VISIBLE);
            if (tvTimelineStep2Title != null) tvTimelineStep2Title.setText("2. Aguardando alguém encontrar o objeto");
            if (tvTimelineStep2Sub != null) tvTimelineStep2Sub.setText("Aguardando localização ou entrega por alguém no campus");
            if (vTimelineStep2Line != null) vTimelineStep2Line.setVisibility(View.GONE);
            if (ivTimelineStep2Icon != null) {
                ivTimelineStep2Icon.setImageResource(R.drawable.ic_clock);
                ivTimelineStep2Icon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_text_tertiary));
            }
            if (layoutTimelineStep3 != null) layoutTimelineStep3.setVisibility(View.GONE);
            if (layoutTimelineStep4 != null) layoutTimelineStep4.setVisibility(View.GONE);

        } else if (isDelivered) {
            tvDetailStatus.setText("Devolvido");
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.fluent_status_delivered_text));
            ivDetailStatusIcon.setImageResource(R.drawable.ic_check);
            ivDetailStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_status_delivered_text));
            btnToggleDelivered.setText("Reabrir ocorrência");

            // Timeline states
            if (layoutTimelineStep2 != null) layoutTimelineStep2.setVisibility(View.VISIBLE);
            if (tvTimelineStep2Title != null) tvTimelineStep2Title.setText("2. Triagem e guarda na Secretaria");
            if (vTimelineStep2Line != null) vTimelineStep2Line.setVisibility(View.VISIBLE);
            if (ivTimelineStep2Icon != null) {
                ivTimelineStep2Icon.setImageResource(R.drawable.ic_check);
                ivTimelineStep2Icon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_status_delivered_text));
            }
            if (layoutTimelineStep3 != null) layoutTimelineStep3.setVisibility(View.VISIBLE);
            if (ivTimelineStep3Icon != null) ivTimelineStep3Icon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_status_delivered_text));
            if (layoutTimelineStep4 != null) layoutTimelineStep4.setVisibility(View.VISIBLE);
            if (ivTimelineStep4Icon != null) ivTimelineStep4Icon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_status_delivered_text));
            if (tvTimelineStep4Title != null) tvTimelineStep4Title.setTextColor(ContextCompat.getColor(this, R.color.fluent_status_delivered_text));
            if (tvTimelineStep4Sub != null) {
                tvTimelineStep4Sub.setText("Devolução concluída com sucesso na Secretaria");
                tvTimelineStep4Sub.setTextColor(ContextCompat.getColor(this, R.color.fluent_text_secondary));
            }

        } else if (isWaitingPickup) {
            tvDetailStatus.setText("Aguardando o dono");
            tvDetailStatus.setTextColor(ContextCompat.getColor(this, R.color.fluent_blue_primary));
            ivDetailStatusIcon.setImageResource(R.drawable.ic_clock);
            ivDetailStatusIcon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_blue_primary));
            btnToggleDelivered.setText("Marcar como devolvido");

            // Timeline states
            if (layoutTimelineStep2 != null) layoutTimelineStep2.setVisibility(View.VISIBLE);
            if (tvTimelineStep2Title != null) tvTimelineStep2Title.setText("2. Triagem e guarda na Secretaria");
            if (vTimelineStep2Line != null) vTimelineStep2Line.setVisibility(View.VISIBLE);
            if (ivTimelineStep2Icon != null) {
                ivTimelineStep2Icon.setImageResource(R.drawable.ic_check);
                ivTimelineStep2Icon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_blue_primary));
            }
            if (layoutTimelineStep3 != null) layoutTimelineStep3.setVisibility(View.VISIBLE);
            if (ivTimelineStep3Icon != null) ivTimelineStep3Icon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_blue_primary));
            if (layoutTimelineStep4 != null) layoutTimelineStep4.setVisibility(View.VISIBLE);
            if (ivTimelineStep4Icon != null) ivTimelineStep4Icon.setColorFilter(ContextCompat.getColor(this, R.color.fluent_text_tertiary));
            if (tvTimelineStep4Title != null) tvTimelineStep4Title.setTextColor(ContextCompat.getColor(this, R.color.fluent_text_secondary));
            if (tvTimelineStep4Sub != null) tvTimelineStep4Sub.setText("Pendente de retirada e assinatura de termo");
        }
    }

    private void setupSmartMatch() {
        if (currentItem == null) {
            cardDetailSmartMatch.setVisibility(View.GONE);
            return;
        }

        if ("LOST".equalsIgnoreCase(currentItem.getType()) && "SEARCHING".equalsIgnoreCase(currentItem.getStatus())) {
            List<ItemModel> matches = repository.findMatchesForLostItem(currentItem);
            if (!matches.isEmpty()) {
                ItemModel topMatch = matches.get(0);
                cardDetailSmartMatch.setVisibility(View.VISIBLE);
                tvDetailSmartMatchText.setText("Encontramos um item guardado na Secretaria: \"" +
                        topMatch.getTitle() + "\" (" + topMatch.getLocation() + ") com características semelhantes!");
                btnOpenDetailMatch.setOnClickListener(v -> {
                    Intent intent = new Intent(this, ItemDetailActivity.class);
                    intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, topMatch.getId());
                    startActivity(intent);
                });
                return;
            }
        }
        cardDetailSmartMatch.setVisibility(View.GONE);
    }

    private void copyProtocolToClipboard() {
        if (currentItem == null) return;
        String code = currentItem.getDisplayProtocolCode();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Protocolo FMP", code);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Protocolo " + code + " copiado para a área de transferência!", Toast.LENGTH_SHORT).show();
        }
    }

    private int getCategoryFallbackIcon(String category) {
        if (category == null) return R.drawable.ic_category_other;
        if (category.contains("Documento")) return R.drawable.ic_category_document;
        if (category.contains("Eletrônic")) return R.drawable.ic_category_electronic;
        if (category.contains("Chave")) return R.drawable.ic_category_key;
        if (category.contains("Material") || category.contains("Livro")) return R.drawable.ic_category_school;
        if (category.contains("Garrafa") || category.contains("Copo")) return R.drawable.ic_category_bottle;
        if (category.contains("Roupa") || category.contains("Agasalho")) return R.drawable.ic_category_clothing;
        return R.drawable.ic_category_other;
    }

    private void showPickupInstructionsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pickup_instructions, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnDismissInstructions).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
