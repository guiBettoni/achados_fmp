package com.example;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.ItemAdapter;
import com.example.data.ItemRepository;
import com.example.data.SessionManager;
import com.example.model.ItemModel;
import com.example.ui.ItemDetailActivity;
import com.example.ui.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private ItemRepository repository;
    private SessionManager sessionManager;

    // Bottom Navigation Elements (Mockup 3-action bar)
    private View btnNavHome;
    private View btnNavAdd;
    private View btnNavProfile;
    private ImageView ivNavHomeIcon;
    private ImageView ivNavProfileIcon;

    // Top Bar
    private ImageView ivThemeToggleIcon;

    // TAB 0: HOME
    private EditText etHomeSearch;
    private ChipGroup chipGroupCategories;
    private RecyclerView rvRecentItems;
    private ItemAdapter recentItemsAdapter;
    private View cardDashboardLost;
    private View cardDashboardFound;
    private TextView tvDashboardLostCount;
    private TextView tvDashboardFoundCount;
    private TextView tvDashboardDeliveredCount;

    // Smart Match Alert on Home
    private View cardSmartMatchAlert;
    private TextView tvSmartMatchDescription;
    private TextView tvSmartMatchItemTitle;
    private View rowSmartMatchAction;
    private View btnDismissSmartMatch;
    private boolean isSmartMatchDismissed = false;

    // TAB 1: ALL ITEMS
    private EditText etAllItemsSearch;
    private ChipGroup chipGroupAllCategories;
    private TextView tvActiveFilterDesc;
    private TextView tvClearFilter;
    private RecyclerView rvAllItems;
    private ItemAdapter allItemsAdapter;
    private View layoutEmptyAllItems;
    private String selectedAllCategory = "Todos";
    private String selectedAllStatus = "Todos";

    // TAB 2: REGISTER (Tela 7, 8, 9)
    private ViewFlipper registerViewFlipper;
    private String currentRegisterType = "FOUND"; // "LOST" or "FOUND"
    private TextView tvFormTitle;
    private TextView lblItemName;
    private TextView lblDate;
    private TextView lblLocation;
    private View layoutFieldFinder;
    private TextInputLayout tilTitle, tilCategory, tilDate, tilLocation, tilDescription, tilObservations;
    private TextInputEditText etTitle, etDate, etFinderName, etDescription, etObservations;
    private AutoCompleteTextView actvCategory, actvLocation;

    // Photo Attachment
    private ActivityResultLauncher<String> photoPickerLauncher;
    private String selectedPhotoPath = null;
    private View cardAddPhoto;
    private View layoutPhotoPrompt;
    private View layoutPhotoSelected;
    private ImageView ivRegisterPhotoThumb;
    private TextView tvPhotoFileName;
    private MaterialButton btnRemovePhoto;

    // TAB 3: MY ITEMS (Tela 11)
    private MaterialButtonToggleGroup toggleMyItemsGroup;
    private RecyclerView rvMyItems;
    private ItemAdapter myItemsAdapter;
    private View layoutEmptyMyItems;
    private String myItemsCurrentType = "LOST"; // Default 'Perdidos' matching Tela 11

    // TAB 4: PROFILE (Tela 10)
    private TextView tvProfileName;
    private TextView tvProfileEmail;

    // TAB 5: SETTINGS (Tela 12)
    private TextView tvCurrentThemeLabel;

    private final String[] categoryOptions = new String[]{
            "Chaves",
            "Documentos",
            "Eletrônicos",
            "Acessórios",
            "Roupas",
            "Livros",
            "Outros"
    };

    private final String[] locationOptions = new String[]{
            "Laboratório 4",
            "Laboratório 2",
            "Sala 08",
            "Sala 12",
            "Bloco A - Sala 202",
            "Biblioteca Central",
            "Auditório",
            "Pátio / Cantina",
            "Estacionamento",
            "Quadra Poliesportiva",
            "Secretaria Acadêmica",
            "Outro Ponto no Campus"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        // Apply saved theme preference
        if (sessionManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        repository = ItemRepository.getInstance(this);

        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        copyImageToInternalStorage(uri);
                    }
                }
        );

        initViews();
        setupBottomNavigation();
        setupTopBarThemeToggle();
        setupHomeTab();
        setupAllItemsTab();
        setupRegisterTab();
        setupMyItemsTab();
        setupProfileTab();
        setupSettingsTab();
        applyGuestRestrictions();
        setupBackPressHandling();
    }

    // Visitante só consulta: nas abas "Cadastrar" e "Meus Registros" mostramos
    // uma mensagem no lugar do conteúdo, com um botão para entrar com conta institucional.
    private void applyGuestRestrictions() {
        if (!sessionManager.isGuest()) return;

        View registerContent = findViewById(R.id.registerViewFlipper);
        View registerGuestBlock = findViewById(R.id.layoutGuestBlockRegister);
        View myItemsContent = findViewById(R.id.layoutMyItemsContent);
        View myItemsGuestBlock = findViewById(R.id.layoutGuestBlockMyItems);

        if (registerContent != null) registerContent.setVisibility(View.GONE);
        if (registerGuestBlock != null) registerGuestBlock.setVisibility(View.VISIBLE);
        if (myItemsContent != null) myItemsContent.setVisibility(View.GONE);
        if (myItemsGuestBlock != null) myItemsGuestBlock.setVisibility(View.VISIBLE);

        View btnFromRegister = findViewById(R.id.btnGuestLoginFromRegister);
        View btnFromMyItems = findViewById(R.id.btnGuestLoginFromMyItems);
        if (btnFromRegister != null) btnFromRegister.setOnClickListener(v -> performLogout());
        if (btnFromMyItems != null) btnFromMyItems.setOnClickListener(v -> performLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAllData();
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);

        btnNavHome = findViewById(R.id.btnNavHome);
        btnNavAdd = findViewById(R.id.btnNavAdd);
        btnNavProfile = findViewById(R.id.btnNavProfile);
        ivNavHomeIcon = findViewById(R.id.ivNavHomeIcon);
        ivNavProfileIcon = findViewById(R.id.ivNavProfileIcon);

        ivThemeToggleIcon = findViewById(R.id.ivThemeToggleIcon);
        updateThemeToggleIcon();
    }

    private void setupBottomNavigation() {
        btnNavHome.setOnClickListener(v -> navigateToScreen(0));

        btnNavAdd.setOnClickListener(v -> {
            registerViewFlipper.setDisplayedChild(0); // Reset to Step 1 (O que aconteceu?)
            navigateToScreen(2);
        });

        btnNavProfile.setOnClickListener(v -> navigateToScreen(4));
    }

    private void navigateToScreen(int screenIndex) {
        viewFlipper.setDisplayedChild(screenIndex);

        // Update Bottom Nav Icons Tint
        int blueColor = ContextCompat.getColor(this, R.color.fluent_blue_primary);
        int secondaryColor = ContextCompat.getColor(this, R.color.fluent_text_secondary);

        if (screenIndex == 0 || screenIndex == 1) {
            ivNavHomeIcon.setColorFilter(blueColor);
            ivNavProfileIcon.setColorFilter(secondaryColor);
        } else if (screenIndex == 4 || screenIndex == 3 || screenIndex == 5) {
            ivNavHomeIcon.setColorFilter(secondaryColor);
            ivNavProfileIcon.setColorFilter(blueColor);
        } else {
            ivNavHomeIcon.setColorFilter(secondaryColor);
            ivNavProfileIcon.setColorFilter(secondaryColor);
        }

        if (screenIndex == 0) refreshHomeTab();
        if (screenIndex == 1) refreshAllItemsTab();
        if (screenIndex == 3) refreshMyItemsTab();
        if (screenIndex == 4) refreshProfileTab();
    }

    private void setupTopBarThemeToggle() {
        findViewById(R.id.btnToggleThemeTop).setOnClickListener(v -> toggleDarkMode());
    }

    private void toggleDarkMode() {
        boolean willBeDark = !sessionManager.isDarkMode();
        sessionManager.setDarkMode(willBeDark);
        AppCompatDelegate.setDefaultNightMode(willBeDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        updateThemeToggleIcon();
        Toast.makeText(this, willBeDark ? "Tema escuro ativado" : "Tema claro ativado", Toast.LENGTH_SHORT).show();
    }

    private void updateThemeToggleIcon() {
        if (ivThemeToggleIcon != null) {
            boolean isDark = sessionManager.isDarkMode();
            ivThemeToggleIcon.setImageResource(isDark ? R.drawable.ic_sun : R.drawable.ic_moon);
        }
    }

    // =========================================================================
    // TAB 0: HOME (Tela 3)
    // =========================================================================
    private void setupHomeTab() {
        etHomeSearch = findViewById(R.id.etHomeSearch);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        rvRecentItems = findViewById(R.id.rvRecentItems);
        TextView tvHomeSeeAll = findViewById(R.id.tvHomeSeeAll);

        cardDashboardLost = findViewById(R.id.cardDashboardLost);
        cardDashboardFound = findViewById(R.id.cardDashboardFound);
        tvDashboardLostCount = findViewById(R.id.tvDashboardLostCount);
        tvDashboardFoundCount = findViewById(R.id.tvDashboardFoundCount);
        tvDashboardDeliveredCount = findViewById(R.id.tvDashboardDeliveredCount);

        if (cardDashboardLost != null) {
            cardDashboardLost.setOnClickListener(v -> {
                selectedAllCategory = "Todos";
                selectedAllStatus = "SEARCHING";
                navigateToScreen(1);
            });
        }

        if (cardDashboardFound != null) {
            cardDashboardFound.setOnClickListener(v -> {
                selectedAllCategory = "Todos";
                selectedAllStatus = "WAITING_PICKUP";
                navigateToScreen(1);
            });
        }

        rvRecentItems.setLayoutManager(new LinearLayoutManager(this));
        recentItemsAdapter = new ItemAdapter(this, this::openItemDetails);
        rvRecentItems.setAdapter(recentItemsAdapter);

        tvHomeSeeAll.setOnClickListener(v -> {
            selectedAllCategory = "Todos";
            selectedAllStatus = "Todos";
            navigateToScreen(1);
        });

        etHomeSearch.setOnEditorActionListener((v, actionId, event) -> {
            String query = etHomeSearch.getText() != null ? etHomeSearch.getText().toString().trim() : "";
            if (!query.isEmpty()) {
                selectedAllCategory = "Todos";
                selectedAllStatus = "Todos";
                navigateToScreen(1);
                etAllItemsSearch.setText(query);
            }
            return false;
        });

        // Category pills on Home
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    String cat = chip.getText().toString();
                    if (!"Todos".equalsIgnoreCase(cat)) {
                        selectedAllCategory = cat;
                        selectedAllStatus = "Todos";
                        navigateToScreen(1);
                    }
                }
            }
        });

        // Smart Match Alert on Home (B5)
        cardSmartMatchAlert = findViewById(R.id.cardSmartMatchAlert);
        tvSmartMatchItemTitle = findViewById(R.id.tvSmartMatchItemTitle);
        rowSmartMatchAction = findViewById(R.id.rowSmartMatchAction);
        btnDismissSmartMatch = findViewById(R.id.btnDismissSmartMatch);

        if (btnDismissSmartMatch != null) {
            btnDismissSmartMatch.setOnClickListener(v -> {
                isSmartMatchDismissed = true;
                if (cardSmartMatchAlert != null) {
                    cardSmartMatchAlert.setVisibility(View.GONE);
                }
            });
        }
    }

    private void refreshHomeTab() {
        List<ItemModel> recents = repository.getRecentFoundItems();
        recentItemsAdapter.updateData(recents);

        if (tvDashboardLostCount != null) {
            tvDashboardLostCount.setText(String.valueOf(repository.getLostCount()));
        }
        if (tvDashboardFoundCount != null) {
            tvDashboardFoundCount.setText(String.valueOf(repository.getFoundCount()));
        }
        if (tvDashboardDeliveredCount != null) {
            int delivered = repository.getDeliveredCount();
            if (delivered > 0) {
                tvDashboardDeliveredCount.setText(delivered + " objetos já foram devolvidos aos donos na Secretaria da FMP.");
            } else {
                tvDashboardDeliveredCount.setText("Central de achados e perdidos oficial no campus da FMP.");
            }
        }

        // B5: ligue o card a ItemRepository.getTopSmartMatchForUser().
        // Com correspondência, mostre o título e o local do item encontrado e abra o detalhe ao tocar.
        // Sem correspondência, mantenha o card escondido.
        if (cardSmartMatchAlert != null) {
            if (isSmartMatchDismissed) {
                cardSmartMatchAlert.setVisibility(View.GONE);
            } else {
                ItemModel topMatch = repository.getTopSmartMatchForUser(sessionManager.getUserEmail());
                if (topMatch != null) {
                    cardSmartMatchAlert.setVisibility(View.VISIBLE);
                    String titleAndLocation = topMatch.getTitle() + " • " + topMatch.getLocation();
                    if (tvSmartMatchItemTitle != null) {
                        tvSmartMatchItemTitle.setText(titleAndLocation);
                    }
                    View.OnClickListener openMatchListener = v -> openItemDetails(topMatch);
                    if (rowSmartMatchAction != null) {
                        rowSmartMatchAction.setOnClickListener(openMatchListener);
                    }
                    cardSmartMatchAlert.setOnClickListener(openMatchListener);
                } else {
                    cardSmartMatchAlert.setVisibility(View.GONE);
                }
            }
        }
    }

    // =========================================================================
    // TAB 1: ALL ITEMS (Tela 4)
    // =========================================================================
    private void setupAllItemsTab() {
        findViewById(R.id.btnAllItemsBack).setOnClickListener(v -> navigateToScreen(0));

        etAllItemsSearch = findViewById(R.id.etAllItemsSearch);
        chipGroupAllCategories = findViewById(R.id.chipGroupAllCategories);
        tvActiveFilterDesc = findViewById(R.id.tvActiveFilterDesc);
        tvClearFilter = findViewById(R.id.tvClearFilter);
        rvAllItems = findViewById(R.id.rvAllItems);
        layoutEmptyAllItems = findViewById(R.id.layoutEmptyAllItems);

        rvAllItems.setLayoutManager(new LinearLayoutManager(this));
        allItemsAdapter = new ItemAdapter(this, this::openItemDetails);
        rvAllItems.setAdapter(allItemsAdapter);

        etAllItemsSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyAllItemsFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.btnOpenFilter).setOnClickListener(v -> showFilterDialog());

        tvClearFilter.setOnClickListener(v -> clearAllItemsFilters());
        findViewById(R.id.btnEmptyResetFilters).setOnClickListener(v -> clearAllItemsFilters());

        // Category pills on All Items (Tela 4)
        chipGroupAllCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                Chip chip = findViewById(checkedId);
                if (chip != null) {
                    selectedAllCategory = chip.getText().toString();
                    applyAllItemsFilters();
                }
            }
        });
    }

    private void applyAllItemsFilters() {
        String query = etAllItemsSearch.getText() != null ? etAllItemsSearch.getText().toString().trim() : "";
        List<ItemModel> filtered = repository.searchAndFilter(query, selectedAllCategory, selectedAllStatus, "ALL");

        allItemsAdapter.updateData(filtered);

        if (filtered.isEmpty()) {
            rvAllItems.setVisibility(View.GONE);
            layoutEmptyAllItems.setVisibility(View.VISIBLE);
        } else {
            rvAllItems.setVisibility(View.VISIBLE);
            layoutEmptyAllItems.setVisibility(View.GONE);
        }

        boolean hasFilter = !"Todos".equalsIgnoreCase(selectedAllCategory)
                || !"Todos".equalsIgnoreCase(selectedAllStatus)
                || !query.isEmpty();

        StringBuilder sb = new StringBuilder("Exibindo ");
        if (!"Todos".equalsIgnoreCase(selectedAllCategory)) {
            sb.append("categoria \"").append(selectedAllCategory).append("\", ");
        }
        if (!"Todos".equalsIgnoreCase(selectedAllStatus)) {
            sb.append(getStatusLabel(selectedAllStatus)).append(", ");
        }
        if (!query.isEmpty()) {
            sb.append("termo \"").append(query).append("\", ");
        }
        if (!hasFilter) {
            sb.append("todos os objetos ");
        }
        sb.append(String.format(Locale.getDefault(), "(%d itens)", filtered.size()));

        tvActiveFilterDesc.setText(sb.toString());
        tvClearFilter.setVisibility(hasFilter ? View.VISIBLE : View.GONE);
    }

    // Converte o código interno do status no texto que o usuário vê.
    private String getStatusLabel(String statusCode) {
        if ("SEARCHING".equalsIgnoreCase(statusCode)) return "Procura-se";
        if ("WAITING_PICKUP".equalsIgnoreCase(statusCode)) return "Aguardando o dono";
        if ("DELIVERED".equalsIgnoreCase(statusCode)) return "Devolvidos";
        return statusCode;
    }

    private void clearAllItemsFilters() {
        etAllItemsSearch.setText("");
        selectedAllCategory = "Todos";
        selectedAllStatus = "Todos";

        // Sync chip
        chipGroupAllCategories.check(R.id.chipAllCatAll);
        applyAllItemsFilters();
    }

    private void refreshAllItemsTab() {
        // Sync chip with selectedAllCategory
        syncAllItemsCategoryChip();
        applyAllItemsFilters();
    }

    private void syncAllItemsCategoryChip() {
        if ("Chaves".equalsIgnoreCase(selectedAllCategory)) {
            chipGroupAllCategories.check(R.id.chipAllCatKeys);
        } else if ("Documentos".equalsIgnoreCase(selectedAllCategory)) {
            chipGroupAllCategories.check(R.id.chipAllCatDocs);
        } else if ("Eletrônicos".equalsIgnoreCase(selectedAllCategory)) {
            chipGroupAllCategories.check(R.id.chipAllCatElec);
        } else if ("Acessórios".equalsIgnoreCase(selectedAllCategory)) {
            chipGroupAllCategories.check(R.id.chipAllCatAccessories);
        } else if ("Roupas".equalsIgnoreCase(selectedAllCategory)) {
            chipGroupAllCategories.check(R.id.chipAllCatClothes);
        } else if ("Livros".equalsIgnoreCase(selectedAllCategory)) {
            chipGroupAllCategories.check(R.id.chipAllCatBooks);
        } else if ("Outros".equalsIgnoreCase(selectedAllCategory)) {
            chipGroupAllCategories.check(R.id.chipAllCatOther);
        } else {
            chipGroupAllCategories.check(R.id.chipAllCatAll);
        }
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ChipGroup chipCategory = dialogView.findViewById(R.id.chipGroupFilterCategory);
        ChipGroup chipStatus = dialogView.findViewById(R.id.chipGroupFilterStatus);
        TextInputEditText etFilterStartDate = dialogView.findViewById(R.id.etFilterStartDate);
        TextInputEditText etFilterEndDate = dialogView.findViewById(R.id.etFilterEndDate);
        MaterialButton btnApply = dialogView.findViewById(R.id.btnDialogApplyFilter);
        MaterialButton btnClear = dialogView.findViewById(R.id.btnDialogClearFilter);
        ImageView btnFilterClose = dialogView.findViewById(R.id.btnFilterClose);

        btnFilterClose.setOnClickListener(v -> dialog.dismiss());

        // Pre-select category chip
        preselectFilterCategory(chipCategory, selectedAllCategory);

        // Pre-select status chip
        if ("WAITING_PICKUP".equalsIgnoreCase(selectedAllStatus)) {
            chipStatus.check(R.id.filterStatusWaiting);
        } else if ("DELIVERED".equalsIgnoreCase(selectedAllStatus)) {
            chipStatus.check(R.id.filterStatusDelivered);
        } else {
            chipStatus.check(R.id.filterStatusAll);
        }

        // Date pickers
        etFilterStartDate.setOnClickListener(v -> showDatePicker(etFilterStartDate));
        etFilterEndDate.setOnClickListener(v -> showDatePicker(etFilterEndDate));

        btnApply.setOnClickListener(v -> {
            int checkedCatId = chipCategory.getCheckedChipId();
            Chip catChip = dialogView.findViewById(checkedCatId);
            if (catChip != null) {
                String catText = catChip.getText().toString();
                selectedAllCategory = catText.contains("Todas") ? "Todos" : catText;
            } else {
                selectedAllCategory = "Todos";
            }

            // Guardamos o código interno (ex.: "WAITING_PICKUP") e não o texto do chip.
            // Assim, se o texto exibido mudar, o filtro continua funcionando.
            int checkedStatId = chipStatus.getCheckedChipId();
            if (checkedStatId == R.id.filterStatusWaiting) {
                selectedAllStatus = "WAITING_PICKUP";
            } else if (checkedStatId == R.id.filterStatusDelivered) {
                selectedAllStatus = "DELIVERED";
            } else {
                selectedAllStatus = "Todos";
            }

            syncAllItemsCategoryChip();
            applyAllItemsFilters();
            dialog.dismiss();
        });

        btnClear.setOnClickListener(v -> {
            clearAllItemsFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void preselectFilterCategory(ChipGroup group, String category) {
        if ("Chaves".equalsIgnoreCase(category)) group.check(R.id.filterCatKeys);
        else if ("Documentos".equalsIgnoreCase(category)) group.check(R.id.filterCatDocs);
        else if ("Eletrônicos".equalsIgnoreCase(category)) group.check(R.id.filterCatElec);
        else if ("Acessórios".equalsIgnoreCase(category)) group.check(R.id.filterCatAccessories);
        else if ("Roupas".equalsIgnoreCase(category)) group.check(R.id.filterCatClothes);
        else if ("Livros".equalsIgnoreCase(category)) group.check(R.id.filterCatBooks);
        else if ("Outros".equalsIgnoreCase(category)) group.check(R.id.filterCatOther);
        else group.check(R.id.filterCatAll);
    }

    // =========================================================================
    // TAB 2: REGISTER OCCURRENCE (Tela 7, 8, 9)
    // =========================================================================
    private void setupRegisterTab() {
        registerViewFlipper = findViewById(R.id.registerViewFlipper);

        // Step 1: Tela 7 - O que aconteceu?
        findViewById(R.id.btnRegisterStep1Back).setOnClickListener(v -> navigateToScreen(0));

        findViewById(R.id.cardOptionPerdi).setOnClickListener(v -> {
            currentRegisterType = "LOST";
            setupRegisterStep2Fields();
            registerViewFlipper.setDisplayedChild(1);
        });

        findViewById(R.id.cardOptionEncontrei).setOnClickListener(v -> {
            currentRegisterType = "FOUND";
            setupRegisterStep2Fields();
            registerViewFlipper.setDisplayedChild(1);
        });

        // Step 2: Tela 8 & 9 - Formulário
        findViewById(R.id.btnRegisterStep2Back).setOnClickListener(v -> registerViewFlipper.setDisplayedChild(0));
        findViewById(R.id.btnCancelRegistration).setOnClickListener(v -> {
            clearSelectedPhoto();
            registerViewFlipper.setDisplayedChild(0);
        });

        tvFormTitle = findViewById(R.id.tvFormTitle);
        lblItemName = findViewById(R.id.lblItemName);
        lblDate = findViewById(R.id.lblDate);
        lblLocation = findViewById(R.id.lblLocation);
        layoutFieldFinder = findViewById(R.id.layoutFieldFinder);

        tilTitle = findViewById(R.id.tilTitle);
        tilCategory = findViewById(R.id.tilCategory);
        tilDate = findViewById(R.id.tilDate);
        tilLocation = findViewById(R.id.tilLocation);
        tilDescription = findViewById(R.id.tilDescription);
        tilObservations = findViewById(R.id.tilObservations);

        // Explicitly disable TextInputLayout floating hint overlay to prevent any text duplication or overlapping
        tilTitle.setHintEnabled(false);
        tilCategory.setHintEnabled(false);
        tilDate.setHintEnabled(false);
        tilLocation.setHintEnabled(false);
        tilDescription.setHintEnabled(false);
        tilObservations.setHintEnabled(false);

        etTitle = findViewById(R.id.etTitle);
        etDate = findViewById(R.id.etDate);
        etFinderName = findViewById(R.id.etFinderName);
        etDescription = findViewById(R.id.etDescription);
        etObservations = findViewById(R.id.etObservations);
        actvCategory = findViewById(R.id.actvCategory);
        actvLocation = findViewById(R.id.actvLocation);

        // Dropdowns
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryOptions);
        actvCategory.setAdapter(catAdapter);

        ArrayAdapter<String> locAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, locationOptions);
        actvLocation.setAdapter(locAdapter);

        // Pre-fill today's date
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        etDate.setText(today);
        etDate.setOnClickListener(v -> showDatePicker(etDate));
        tilDate.setEndIconOnClickListener(v -> showDatePicker(etDate));

        // Photo Attachment (B3)
        cardAddPhoto = findViewById(R.id.cardAddPhoto);
        layoutPhotoPrompt = findViewById(R.id.layoutPhotoPrompt);
        layoutPhotoSelected = findViewById(R.id.layoutPhotoSelected);
        ivRegisterPhotoThumb = findViewById(R.id.ivRegisterPhotoThumb);
        tvPhotoFileName = findViewById(R.id.tvPhotoFileName);
        btnRemovePhoto = findViewById(R.id.btnRemovePhoto);

        if (cardAddPhoto != null) {
            cardAddPhoto.setOnClickListener(v -> {
                if (photoPickerLauncher != null) {
                    photoPickerLauncher.launch("image/*");
                }
            });
        }

        if (btnRemovePhoto != null) {
            btnRemovePhoto.setOnClickListener(v -> clearSelectedPhoto());
        }

        findViewById(R.id.btnSubmitOccurrence).setOnClickListener(v -> submitOccurrence());
    }

    private void setupRegisterStep2Fields() {
        clearSelectedPhoto();
        tilTitle.setError(null);
        tilCategory.setError(null);
        tilDate.setError(null);
        tilLocation.setError(null);
        tilDescription.setError(null);
        tilObservations.setError(null);

        // Guarantee TextInputLayout never draws a floating hint or duplicate text
        tilTitle.setHint(null);
        tilLocation.setHint(null);
        tilTitle.setHintEnabled(false);
        tilLocation.setHintEnabled(false);
        tilCategory.setHintEnabled(false);
        tilDate.setHintEnabled(false);
        tilDescription.setHintEnabled(false);
        tilObservations.setHintEnabled(false);

        // Clear previous input text so new registration starts fresh
        etTitle.setText("");
        actvCategory.setText("", false);
        actvLocation.setText("", false);
        etFinderName.setText("");
        etDescription.setText("");
        etObservations.setText("");

        // Pre-fill today's date
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        etDate.setText(today);

        if ("LOST".equalsIgnoreCase(currentRegisterType)) {
            // Tela 8 - Cadastrar objeto perdido
            tvFormTitle.setText("Cadastrar objeto perdido");
            lblItemName.setText("Nome do objeto");
            etTitle.setHint("Ex.: Carteira, Fone de ouvido, Celular...");
            lblDate.setText("Data da perda");
            lblLocation.setText("Local da perda");
            actvLocation.setHint("Ex.: Sala 08, Biblioteca, Laboratório...");
            layoutFieldFinder.setVisibility(View.GONE);
        } else {
            // Tela 9 - Cadastrar objeto encontrado
            tvFormTitle.setText("Cadastrar objeto encontrado");
            lblItemName.setText("Nome do objeto");
            etTitle.setHint("Ex.: Chave, Garrafa, Livro, Mochila...");
            lblDate.setText("Data que encontrou");
            lblLocation.setText("Local onde encontrou");
            actvLocation.setHint("Ex.: Auditório, Laboratório 4, Pátio...");
            layoutFieldFinder.setVisibility(View.VISIBLE);
        }
    }

    private void copyImageToInternalStorage(Uri sourceUri) {
        try {
            File photosDir = new File(getFilesDir(), "item_photos");
            if (!photosDir.exists()) {
                photosDir.mkdirs();
            }
            String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
            File destFile = new File(photosDir, fileName);

            try (InputStream in = getContentResolver().openInputStream(sourceUri);
                 OutputStream out = new FileOutputStream(destFile)) {
                if (in == null) return;
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            selectedPhotoPath = destFile.getAbsolutePath();

            if (layoutPhotoPrompt != null) layoutPhotoPrompt.setVisibility(View.GONE);
            if (layoutPhotoSelected != null) layoutPhotoSelected.setVisibility(View.VISIBLE);
            if (ivRegisterPhotoThumb != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(selectedPhotoPath);
                ivRegisterPhotoThumb.setImageBitmap(bitmap);
            }
            if (tvPhotoFileName != null) {
                tvPhotoFileName.setText(fileName);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível carregar a imagem selecionada.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSelectedPhoto() {
        selectedPhotoPath = null;
        if (layoutPhotoSelected != null) layoutPhotoSelected.setVisibility(View.GONE);
        if (layoutPhotoPrompt != null) layoutPhotoPrompt.setVisibility(View.VISIBLE);
        if (ivRegisterPhotoThumb != null) ivRegisterPhotoThumb.setImageDrawable(null);
        if (tvPhotoFileName != null) tvPhotoFileName.setText("");
    }

    private void submitOccurrence() {
        tilTitle.setError(null);
        tilCategory.setError(null);
        tilDate.setError(null);
        tilLocation.setError(null);
        tilDescription.setError(null);
        tilObservations.setError(null);

        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String category = actvCategory.getText() != null ? actvCategory.getText().toString().trim() : "";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String location = actvLocation.getText() != null ? actvLocation.getText().toString().trim() : "";
        String finder = etFinderName.getText() != null ? etFinderName.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String observations = etObservations.getText() != null ? etObservations.getText().toString().trim() : "";

        boolean hasError = false;

        if (TextUtils.isEmpty(title)) {
            tilTitle.setError("Informe o nome do objeto");
            hasError = true;
        }

        if (TextUtils.isEmpty(category)) {
            tilCategory.setError("Selecione a categoria");
            hasError = true;
        }

        if (TextUtils.isEmpty(date)) {
            tilDate.setError("Informe a data");
            hasError = true;
        }

        if (TextUtils.isEmpty(location)) {
            tilLocation.setError("Informe o local");
            hasError = true;
        }

        if (TextUtils.isEmpty(description)) {
            tilDescription.setError("Informe a descrição do objeto");
            hasError = true;
        }

        if (hasError) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isLost = "LOST".equalsIgnoreCase(currentRegisterType);
        String status = isLost ? "SEARCHING" : "WAITING_PICKUP";
        String registeredByName = !finder.isEmpty() ? finder : sessionManager.getUserName();
        String holdingLocation = isLost ? "" : "Secretaria";

        // B2: Itens novos sem foto devem usar o ícone da categoria (photoRes = 0)
        int photoRes = 0;

        ItemModel newItem = new ItemModel(
                "fmp-" + System.currentTimeMillis(),
                title,
                category,
                currentRegisterType,
                date,
                location,
                description,
                observations,
                status,
                sessionManager.getUserEmail(),
                registeredByName,
                holdingLocation,
                photoRes,
                System.currentTimeMillis()
        );

        if (selectedPhotoPath != null) {
            newItem.setPhotoPath(selectedPhotoPath);
        }

        repository.addItem(newItem);

        // Clear inputs and selected photo
        etTitle.setText("");
        actvCategory.setText("", false);
        actvLocation.setText("", false);
        etFinderName.setText("");
        etDescription.setText("");
        etObservations.setText("");
        clearSelectedPhoto();

        String alertTitle = isLost ? "Perda registrada" : "Leve o objeto até a Secretaria";
        String alertMessage = isLost
                ? "Confira a tela inicial: se alguém cadastrar um objeto parecido, um aviso aparecerá lá. Você também pode procurar a Secretaria."
                : getString(R.string.pickup_location_desc);

        new AlertDialog.Builder(this)
                .setTitle(alertTitle)
                .setMessage(alertMessage)
                .setCancelable(false)
                .setPositiveButton("Entendi", (dialog, which) -> {
                    // Switch to Tela 11 (Meus Registros) showing the tab corresponding to the new item
                    myItemsCurrentType = currentRegisterType;
                    if (toggleMyItemsGroup != null) {
                        toggleMyItemsGroup.check("LOST".equalsIgnoreCase(currentRegisterType) ? R.id.btnMyItemsLost : R.id.btnMyItemsFound);
                    }
                    navigateToScreen(3);
                })
                .show();
    }

    // =========================================================================
    // TAB 3: MY ITEMS (Tela 11)
    // =========================================================================
    private void setupMyItemsTab() {
        findViewById(R.id.btnMyItemsBack).setOnClickListener(v -> navigateToScreen(4));
        findViewById(R.id.btnMyItemsSettings).setOnClickListener(v -> navigateToScreen(5));

        toggleMyItemsGroup = findViewById(R.id.toggleMyItemsGroup);
        rvMyItems = findViewById(R.id.rvMyItems);
        layoutEmptyMyItems = findViewById(R.id.layoutEmptyMyItems);

        rvMyItems.setLayoutManager(new LinearLayoutManager(this));
        myItemsAdapter = new ItemAdapter(this, this::openItemDetails);
        rvMyItems.setAdapter(myItemsAdapter);

        toggleMyItemsGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                myItemsCurrentType = (checkedId == R.id.btnMyItemsLost) ? "LOST" : "FOUND";
                refreshMyItemsTab();
            }
        });

        findViewById(R.id.btnEmptyGoRegister).setOnClickListener(v -> {
            registerViewFlipper.setDisplayedChild(0);
            navigateToScreen(2);
        });
    }

    private void refreshMyItemsTab() {
        List<ItemModel> myItems = repository.getMyItems(sessionManager.getUserEmail(), myItemsCurrentType);
        myItemsAdapter.updateData(myItems);

        if (myItems.isEmpty()) {
            rvMyItems.setVisibility(View.GONE);
            layoutEmptyMyItems.setVisibility(View.VISIBLE);
        } else {
            rvMyItems.setVisibility(View.VISIBLE);
            layoutEmptyMyItems.setVisibility(View.GONE);
        }
    }

    // =========================================================================
    // TAB 4: PROFILE (Tela 10)
    // =========================================================================
    private void setupProfileTab() {
        findViewById(R.id.btnProfileBack).setOnClickListener(v -> navigateToScreen(0));
        findViewById(R.id.btnProfileSettingsTop).setOnClickListener(v -> navigateToScreen(5));

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        // Row Meus registros -> Screen 3
        findViewById(R.id.rowMyRecords).setOnClickListener(v -> {
            myItemsCurrentType = "LOST";
            toggleMyItemsGroup.check(R.id.btnMyItemsLost);
            navigateToScreen(3);
        });

        // Row Objetos perdidos -> Screen 3 (Perdidos)
        findViewById(R.id.rowLostItems).setOnClickListener(v -> {
            myItemsCurrentType = "LOST";
            toggleMyItemsGroup.check(R.id.btnMyItemsLost);
            navigateToScreen(3);
        });

        // Row Objetos encontrados -> Screen 3 (Encontrados)
        findViewById(R.id.rowFoundItems).setOnClickListener(v -> {
            myItemsCurrentType = "FOUND";
            toggleMyItemsGroup.check(R.id.btnMyItemsFound);
            navigateToScreen(3);
        });

        // Row Configurações -> Screen 5
        findViewById(R.id.rowSettings).setOnClickListener(v -> navigateToScreen(5));

        // Row Sobre o aplicativo
        findViewById(R.id.rowAbout).setOnClickListener(v -> showAboutDialog());

        // Row Sair da conta
        findViewById(R.id.rowLogout).setOnClickListener(v -> performLogout());
    }

    private void refreshProfileTab() {
        tvProfileName.setText(sessionManager.getUserName());
        tvProfileEmail.setText(sessionManager.getUserEmail());
    }

    // =========================================================================
    // TAB 5: SETTINGS (Tela 12)
    // =========================================================================
    private void setupSettingsTab() {
        findViewById(R.id.btnSettingsBack).setOnClickListener(v -> navigateToScreen(4));

        tvCurrentThemeLabel = findViewById(R.id.tvCurrentThemeLabel);
        tvCurrentThemeLabel.setText(sessionManager.isDarkMode() ? "Escuro" : "Claro");
        findViewById(R.id.rowAppTheme).setOnClickListener(v -> showThemeChooserDialog());

        findViewById(R.id.rowPrivacyPolicy).setOnClickListener(v -> showPrivacyDialog());
        findViewById(R.id.rowTermsOfUse).setOnClickListener(v -> showTermsDialog());
    }

    private void showThemeChooserDialog() {
        String[] options = new String[]{"Tema Claro", "Tema Escuro"};
        int checkedItem = sessionManager.isDarkMode() ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("Tema do aplicativo")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    boolean dark = (which == 1);
                    sessionManager.setDarkMode(dark);
                    tvCurrentThemeLabel.setText(dark ? "Escuro" : "Claro");
                    AppCompatDelegate.setDefaultNightMode(dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
                    updateThemeToggleIcon();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDatePicker(TextInputEditText target) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selected = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    target.setText(selected);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Achados da FMP")
                .setMessage("Aplicativo desenvolvido para a Faculdade Municipal de Palhoça com o objetivo de centralizar o registro e a consulta de objetos perdidos e achados no campus.\n\n"
                        + "Identidade inspirada no Microsoft Fluent Design System.")
                .setPositiveButton("Fechar", null)
                .show();
    }

    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Política de Privacidade")
                .setMessage("• O aplicativo coleta apenas dados necessários para rastreabilidade de ocorrências acadêmicas.\n"
                        + "• Características confidenciais de itens não são divulgadas publicamente para resguardar os proprietários.\n"
                        + "• A validação é realizada presencialmente na Secretaria da FMP.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Termos de Uso")
                .setMessage("Ao utilizar o Achados da FMP, você concorda em registrar apenas informações verídicas sobre itens perdidos ou encontrados nas dependências da faculdade.\n\n"
                        + "Todo item encontrado deve ser entregue na Secretaria Acadêmica para guarda responsável.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void performLogout() {
        sessionManager.logout();
        Toast.makeText(this, "Sessão encerrada", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private void openItemDetails(ItemModel item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    private void setupBackPressHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int current = viewFlipper.getDisplayedChild();
                if (current == 2 && registerViewFlipper.getDisplayedChild() == 1) {
                    registerViewFlipper.setDisplayedChild(0);
                } else if (current != 0) {
                    navigateToScreen(0);
                } else {
                    finish();
                }
            }
        });
    }

    private void refreshAllData() {
        refreshHomeTab();
        refreshAllItemsTab();
        refreshMyItemsTab();
        refreshProfileTab();
    }
}
