package org.telegram.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

// ★奶龙客户端: 高级设置(文件夹结构). category=0顶层列分类, 其余为具体分类的开关页
public class NaiLongSettingsActivity extends BaseFragment {

    private RecyclerListView listView;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CHECK = 1;
    private static final int VIEW_TYPE_SHADOW = 2;
    private static final int VIEW_TYPE_FOLDER = 3;

    // 分类(文件夹)
    private static final int CAT_ROOT = 0;
    private static final int CAT_MESSAGE = 1;
    private static final int CAT_PRIVACY = 2;
    private static final int CAT_CLEAN = 3;

    // 开关id
    private static final int ID_SHOW_DELETED = 1;
    private static final int ID_SHOW_EDITED = 2;
    private static final int ID_DISABLE_SECURE = 3;
    private static final int ID_ALLOW_SAVE = 4;
    private static final int ID_NO_SPONSORED = 5;

    private final int category;

    public NaiLongSettingsActivity() {
        this(CAT_ROOT);
    }

    public NaiLongSettingsActivity(int category) {
        this.category = category;
    }

    private static String catName(int cat) {
        switch (cat) {
            case CAT_MESSAGE: return "消息类";
            case CAT_PRIVACY: return "隐私与安全";
            case CAT_CLEAN: return "净化";
            default: return "高级设置";
        }
    }

    private final ArrayList<Item> items = new ArrayList<>();

    private static class Item {
        final int viewType;
        final int id;          // 开关id / 分类id
        final CharSequence text;
        final CharSequence value; // 文件夹副标题
        Item(int viewType, int id, CharSequence text, CharSequence value) {
            this.viewType = viewType;
            this.id = id;
            this.text = text;
            this.value = value;
        }
    }

    private void buildItems() {
        items.clear();
        if (category == CAT_ROOT) {
            items.add(new Item(VIEW_TYPE_HEADER, 0, "功能分类", null));
            items.add(new Item(VIEW_TYPE_FOLDER, CAT_MESSAGE, "消息类", "防撤回 / 无视编辑"));
            items.add(new Item(VIEW_TYPE_FOLDER, CAT_PRIVACY, "隐私与安全", "去截图 / 破解转发保存"));
            items.add(new Item(VIEW_TYPE_FOLDER, CAT_CLEAN, "净化", "去除频道广告"));
            items.add(new Item(VIEW_TYPE_SHADOW, 0, "更多功能(下载加速/界面美化/频道群组等)陆续加入各分类。", null));
        } else if (category == CAT_MESSAGE) {
            items.add(new Item(VIEW_TYPE_HEADER, 0, "消息类", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_SHOW_DELETED, "防撤回(保留被撤回的消息)", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_SHOW_EDITED, "无视编辑(保留全部编辑历史)", null));
            items.add(new Item(VIEW_TYPE_SHADOW, 0, "防撤回: 别人双向删除(delete for everyone)的消息也会保留并标\"已删除\"。\n无视编辑: 每次编辑前的原文都保留, 消息下方列出全部编辑历史。", null));
        } else if (category == CAT_PRIVACY) {
            items.add(new Item(VIEW_TYPE_HEADER, 0, "隐私与安全", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_DISABLE_SECURE, "去除截图限制", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_ALLOW_SAVE, "破解转发/保存限制", null));
            items.add(new Item(VIEW_TYPE_SHADOW, 0, "允许对禁止转发/保存的聊天截图、转发并保存其中内容。", null));
        } else if (category == CAT_CLEAN) {
            items.add(new Item(VIEW_TYPE_HEADER, 0, "净化", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_NO_SPONSORED, "去除频道广告", null));
            items.add(new Item(VIEW_TYPE_SHADOW, 0, "隐藏频道里的官方推广(广告)消息。", null));
        }
    }

    private boolean getValue(int id) {
        switch (id) {
            case ID_SHOW_DELETED: return SharedConfig.nailongShowDeleted;
            case ID_SHOW_EDITED: return SharedConfig.nailongShowEdited;
            case ID_DISABLE_SECURE: return SharedConfig.nailongDisableFlagSecure;
            case ID_ALLOW_SAVE: return SharedConfig.nailongAllowSaveRestricted;
            case ID_NO_SPONSORED: return SharedConfig.nailongNoSponsored;
        }
        return false;
    }

    private void toggle(int id) {
        switch (id) {
            case ID_SHOW_DELETED: SharedConfig.nailongShowDeleted = !SharedConfig.nailongShowDeleted; break;
            case ID_SHOW_EDITED: SharedConfig.nailongShowEdited = !SharedConfig.nailongShowEdited; break;
            case ID_DISABLE_SECURE: SharedConfig.nailongDisableFlagSecure = !SharedConfig.nailongDisableFlagSecure; break;
            case ID_ALLOW_SAVE: SharedConfig.nailongAllowSaveRestricted = !SharedConfig.nailongAllowSaveRestricted; break;
            case ID_NO_SPONSORED: SharedConfig.nailongNoSponsored = !SharedConfig.nailongNoSponsored; break;
        }
        SharedConfig.saveConfig();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(catName(category));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        buildItems();

        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(new ListAdapter());
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= items.size()) {
                return;
            }
            Item item = items.get(position);
            if (item.viewType == VIEW_TYPE_FOLDER) {
                presentFragment(new NaiLongSettingsActivity(item.id));
            } else if (item.viewType == VIEW_TYPE_CHECK) {
                toggle(item.id);
                if (view instanceof TextCheckCell) {
                    ((TextCheckCell) view).setChecked(getValue(item.id));
                }
            }
        });

        return fragmentView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int t = holder.getItemViewType();
            return t == VIEW_TYPE_CHECK || t == VIEW_TYPE_FOLDER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_HEADER) {
                view = new HeaderCell(getContext());
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            } else if (viewType == VIEW_TYPE_CHECK) {
                view = new TextCheckCell(getContext());
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            } else if (viewType == VIEW_TYPE_FOLDER) {
                view = new TextSettingsCell(getContext());
                view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            } else {
                view = new TextInfoPrivacyCell(getContext());
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position < 0 || position >= items.size()) {
                return;
            }
            Item item = items.get(position);
            boolean divider = position + 1 < items.size() && items.get(position + 1).viewType == item.viewType;
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_HEADER:
                    ((HeaderCell) holder.itemView).setText(item.text);
                    break;
                case VIEW_TYPE_CHECK:
                    ((TextCheckCell) holder.itemView).setTextAndCheck(item.text, getValue(item.id), divider);
                    break;
                case VIEW_TYPE_FOLDER:
                    ((TextSettingsCell) holder.itemView).setTextAndValue(item.text, item.value, divider);
                    break;
                case VIEW_TYPE_SHADOW:
                default:
                    TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                    if (TextUtils.isEmpty(item.text)) {
                        cell.setText(null);
                    } else {
                        cell.setText(item.text);
                    }
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 0 || position >= items.size()) {
                return VIEW_TYPE_SHADOW;
            }
            return items.get(position).viewType;
        }
    }
}
