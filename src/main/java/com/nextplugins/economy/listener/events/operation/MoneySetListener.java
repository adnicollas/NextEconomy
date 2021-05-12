package com.nextplugins.economy.listener.events.operation;

import com.nextplugins.economy.NextEconomy;
import com.nextplugins.economy.api.event.operations.MoneySetEvent;
import com.nextplugins.economy.api.model.account.storage.AccountStorage;
import com.nextplugins.economy.configuration.MessageValue;
import com.nextplugins.economy.util.NumberUtils;
import lombok.val;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class MoneySetListener implements Listener {

    protected final AccountStorage accountStorage = NextEconomy.getInstance().getAccountStorage();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSet(MoneySetEvent event) {

        if (event.isCancelled()) return;

        val sender = event.getSender();
        val target = event.getTarget();
        val amount = event.getAmount();

        val targetAccount = accountStorage.findAccount(target.getName(), target.isOnline());
        if (targetAccount == null) {

            sender.sendMessage(MessageValue.get(MessageValue::invalidTarget));
            return;

        }

        if (Double.isNaN(amount) || amount < 1) {

            sender.sendMessage(MessageValue.get(MessageValue::invalidMoney));
            return;

        }

        targetAccount.setBalance(amount);

        sender.sendMessage(MessageValue.get(MessageValue::setAmount)
                .replace("$player", target.getName())
                .replace("$amount", NumberUtils.format(amount))
        );
    }

}
