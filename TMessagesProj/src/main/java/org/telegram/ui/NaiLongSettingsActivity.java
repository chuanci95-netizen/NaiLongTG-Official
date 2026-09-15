package org.telegram.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.TLRPC;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
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
    private static final int VIEW_TYPE_SELECT = 4;

    // 下载加速档位名(index=SharedConfig.nailongDownloadSpeed)
    private static final String[] DL_NAMES = {"关闭", "4倍加速", "12倍加速", "24倍加速", "极限加速"};

    // 分类(文件夹)
    private static final int CAT_ROOT = 0;
    private static final int CAT_MESSAGE = 1;
    private static final int CAT_PRIVACY = 2;
    private static final int CAT_CLEAN = 3;
    private static final int CAT_DOWNLOAD = 4;
    private static final int CAT_UNLOCK = 5;
    private static final int CAT_PROFILE = 6;

    // 开关id
    private static final int ID_SHOW_DELETED = 1;
    private static final int ID_SHOW_EDITED = 2;
    private static final int ID_DISABLE_SECURE = 3;
    private static final int ID_ALLOW_SAVE = 4;
    private static final int ID_NO_SPONSORED = 5;
    private static final int ID_DOWNLOAD_SPEED = 6;
    private static final int ID_HIDE_TYPING = 7;
    private static final int ID_HIDE_ONLINE = 8;
    private static final int ID_SECONDS_TS = 9;
    private static final int ID_UNLOCK_LIMITS = 10;
    private static final int ID_HIDE_READ = 11;
    private static final int ID_CUSTOM_PHONE = 12;
    private static final int ID_NO_PULL_NEXT = 13;
    private static final int ID_READ_ALL = 14;

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
            case CAT_DOWNLOAD: return "下载与媒体";
            case CAT_UNLOCK: return "解锁增强";
            case CAT_PROFILE: return "个人资料美化";
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
            items.add(new Item(VIEW_TYPE_FOLDER, CAT_MESSAGE, "消息类", null));
            items.add(new Item(VIEW_TYPE_FOLDER, CAT_UNLOCK, "功能增强", null));
            items.add(new Item(VIEW_TYPE_FOLDER, CAT_DOWNLOAD, "下载与媒体", null));
            items.add(new Item(VIEW_TYPE_FOLDER, CAT_PRIVACY, "隐私与安全", null));
            items.add(new Item(VIEW_TYPE_FOLDER, CAT_PROFILE, "个人资料美化", null));
        } else if (category == CAT_MESSAGE) {
            items.add(new Item(VIEW_TYPE_HEADER, 0, "消息类", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_SHOW_DELETED, "防撤回", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_SHOW_EDITED, "无视编辑", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_SECONDS_TS, "精确到秒时间戳", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_NO_PULL_NEXT, "禁止下滑跳转下一个频道", null));
            items.add(new Item(VIEW_TYPE_SELECT, ID_READ_ALL, "一键已读所有对话", null));
        } else if (category == CAT_UNLOCK) {
            items.add(new Item(VIEW_TYPE_HEADER, 0, "功能增强", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_UNLOCK_LIMITS, "突破各种上限", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_NO_SPONSORED, "去除频道广告", null));
        } else if (category == CAT_DOWNLOAD) {
            int lv = SharedConfig.nailongDownloadSpeed;
            if (lv < 0 || lv >= DL_NAMES.length) lv = 0;
            items.add(new Item(VIEW_TYPE_HEADER, 0, "下载与媒体", null));
            items.add(new Item(VIEW_TYPE_SELECT, ID_DOWNLOAD_SPEED, "下载加速", DL_NAMES[lv]));
        } else if (category == CAT_PRIVACY) {
            items.add(new Item(VIEW_TYPE_HEADER, 0, "隐私与安全", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_DISABLE_SECURE, "去除截图限制", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_ALLOW_SAVE, "破解转发/保存限制", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_HIDE_TYPING, "隐藏正在输入", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_HIDE_ONLINE, "隐藏在线状态", null));
            items.add(new Item(VIEW_TYPE_CHECK, ID_HIDE_READ, "隐藏已读回执", null));
        } else if (category == CAT_PROFILE) {
            String cp = SharedConfig.nailongCustomPhone;
            items.add(new Item(VIEW_TYPE_HEADER, 0, "个人资料美化", null));
            items.add(new Item(VIEW_TYPE_SELECT, ID_CUSTOM_PHONE, "自定义手机号", TextUtils.isEmpty(cp) ? "未设置" : cp));
        }
    }

    private boolean getValue(int id) {
        switch (id) {
            case ID_SHOW_DELETED: return SharedConfig.nailongShowDeleted;
            case ID_SHOW_EDITED: return SharedConfig.nailongShowEdited;
            case ID_DISABLE_SECURE: return SharedConfig.nailongDisableFlagSecure;
            case ID_ALLOW_SAVE: return SharedConfig.nailongAllowSaveRestricted;
            case ID_NO_SPONSORED: return SharedConfig.nailongNoSponsored;
            case ID_HIDE_TYPING: return SharedConfig.nailongHideTyping;
            case ID_HIDE_ONLINE: return SharedConfig.nailongHideOnline;
            case ID_SECONDS_TS: return SharedConfig.nailongSecondsTimestamp;
            case ID_UNLOCK_LIMITS: return SharedConfig.nailongUnlockLimits;
            case ID_HIDE_READ: return SharedConfig.nailongHideRead;
            case ID_NO_PULL_NEXT: return SharedConfig.nailongNoPullNextChannel;
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
            case ID_HIDE_TYPING: SharedConfig.nailongHideTyping = !SharedConfig.nailongHideTyping; break;
            case ID_HIDE_ONLINE: SharedConfig.nailongHideOnline = !SharedConfig.nailongHideOnline; break;
            case ID_SECONDS_TS: SharedConfig.nailongSecondsTimestamp = !SharedConfig.nailongSecondsTimestamp; break;
            case ID_UNLOCK_LIMITS: SharedConfig.nailongUnlockLimits = !SharedConfig.nailongUnlockLimits; break;
            case ID_HIDE_READ: SharedConfig.nailongHideRead = !SharedConfig.nailongHideRead; break;
            case ID_NO_PULL_NEXT: SharedConfig.nailongNoPullNextChannel = !SharedConfig.nailongNoPullNextChannel; break;
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
            } else if (item.viewType == VIEW_TYPE_SELECT) {
                if (item.id == ID_DOWNLOAD_SPEED) {
                    showDownloadSpeedDialog();
                } else if (item.id == ID_CUSTOM_PHONE) {
                    showCustomPhoneDialog();
                } else if (item.id == ID_READ_ALL) {
                    markAllDialogsRead();
                }
            }
        });

        return fragmentView;
    }

    private void showDownloadSpeedDialog() {
        if (getParentActivity() == null) {
            return;
        }
        AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
        b.setTitle("下载加速档位");
        b.setItems(DL_NAMES, (dialog, which) -> {
            SharedConfig.nailongDownloadSpeed = which;
            SharedConfig.saveConfig();
            buildItems();
            if (listView != null && listView.getAdapter() != null) {
                listView.getAdapter().notifyDataSetChanged();
            }
        });
        b.setNegativeButton("取消", null);
        showDialog(b.create());
    }

    private void showCustomPhoneDialog() {
        if (getParentActivity() == null) {
            return;
        }
        final EditText editText = new EditText(getParentActivity());
        editText.setText(SharedConfig.nailongCustomPhone == null ? "" : SharedConfig.nailongCustomPhone);
        editText.setHint("留空 = 显示真实手机号");
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        editText.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(6), AndroidUtilities.dp(22), AndroidUtilities.dp(6));
        AlertDialog.Builder b = new AlertDialog.Builder(getParentActivity());
        b.setTitle("自定义手机号显示");
        b.setView(editText);
        b.setPositiveButton("保存", (dialog, which) -> {
            SharedConfig.nailongCustomPhone = editText.getText().toString().trim();
            SharedConfig.saveConfig();
            buildItems();
            if (listView != null && listView.getAdapter() != null) {
                listView.getAdapter().notifyDataSetChanged();
            }
        });
        b.setNegativeButton("取消", null);
        showDialog(b.create());
    }

    private void markAllDialogsRead() {
        try {
            java.util.ArrayList<TLRPC.Dialog> dialogs = getMessagesController().getAllDialogs();
            int n = 0;
            for (int i = 0; i < dialogs.size(); i++) {
                TLRPC.Dialog d = dialogs.get(i);
                if (d == null || d.id == 0 || d.unread_count <= 0 || d instanceof TLRPC.TL_dialogFolder) {
                    continue;
                }
                getMessagesController().markDialogAsRead(d.id, d.top_message, d.top_message, d.last_message_date, false, 0, 0, true, 0);
                n++;
            }
            if (getParentActivity() != null) {
                Toast.makeText(getParentActivity(), "已把 " + n + " 个对话标为已读", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            org.telegram.messenger.FileLog.e(e);
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int t = holder.getItemViewType();
            return t == VIEW_TYPE_CHECK || t == VIEW_TYPE_FOLDER || t == VIEW_TYPE_SELECT;
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
            } else if (viewType == VIEW_TYPE_FOLDER || viewType == VIEW_TYPE_SELECT) {
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
                case VIEW_TYPE_SELECT:
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
