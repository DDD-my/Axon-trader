/*
 * Copyright (c) 2011. Gridshore
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.samples.trader.app.command.trading;

import org.axonframework.domain.AggregateIdentifier;
import org.axonframework.domain.UUIDAggregateIdentifier;
import org.axonframework.samples.trader.app.api.portfolio.AddItemsToPortfolioCommand;
import org.axonframework.samples.trader.app.api.portfolio.CreatePortfolioCommand;
import org.axonframework.samples.trader.app.api.portfolio.ItemsAddedToPortfolioEvent;
import org.axonframework.samples.trader.app.api.portfolio.PortfolioCreatedEvent;
import org.axonframework.samples.trader.app.api.portfolio.reservation.*;
import org.axonframework.test.FixtureConfiguration;
import org.axonframework.test.Fixtures;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Jettro Coenradie
 */
public class PortfolioCommandHandlerTest {
    private FixtureConfiguration fixture;

    @Before
    public void setUp() {
        fixture = Fixtures.newGivenWhenThenFixture();
        PortfolioCommandHandler commandHandler = new PortfolioCommandHandler();
        commandHandler.setRepository(fixture.createGenericRepository(Portfolio.class));
        fixture.registerAnnotatedCommandHandler(commandHandler);
    }

    @Test
    public void testCreatePortfolio() {
        AggregateIdentifier userIdentifier = new UUIDAggregateIdentifier();
        CreatePortfolioCommand command = new CreatePortfolioCommand(userIdentifier);
        fixture.given()
                .when(command)
                .expectEvents(new PortfolioCreatedEvent(userIdentifier));
    }

    /* Items related test methods */
    @Test
    public void testAddItemsToPortfolio() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier orderbookIdentifier = new UUIDAggregateIdentifier();

        AddItemsToPortfolioCommand command = new AddItemsToPortfolioCommand(portfolioIdentifier, orderbookIdentifier, 100);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()))
                .when(command)
                .expectEvents(new ItemsAddedToPortfolioEvent(orderbookIdentifier, 100));
    }

    @Test
    public void testReserveItems_noItemsAvailable() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier orderbookIdentifier = new UUIDAggregateIdentifier();

        ReserveItemsCommand command = new ReserveItemsCommand(portfolioIdentifier, orderbookIdentifier, 200);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()))
                .when(command)
                .expectEvents(new ItemToReserveNotAvailableInPortfolioEvent(orderbookIdentifier));
    }

    @Test
    public void testReserveItems_notEnoughItemsAvailable() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier orderbookIdentifier = new UUIDAggregateIdentifier();

        ReserveItemsCommand command = new ReserveItemsCommand(portfolioIdentifier, orderbookIdentifier, 200);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()),
                new ItemsAddedToPortfolioEvent(orderbookIdentifier, 100))
                .when(command)
                .expectEvents(new NotEnoughItemsAvailableToReserveInPortfolio(orderbookIdentifier, 100, 200));
    }

    @Test
    public void testReserveItems() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier orderbookIdentifier = new UUIDAggregateIdentifier();

        ReserveItemsCommand command = new ReserveItemsCommand(portfolioIdentifier, orderbookIdentifier, 200);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()),
                new ItemsAddedToPortfolioEvent(orderbookIdentifier, 400))
                .when(command)
                .expectEvents(new ItemsReservedEvent(orderbookIdentifier, 200));
    }

    @Test
    public void testConfirmationOfReservation() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier itemIdentifier = new UUIDAggregateIdentifier();

        ConfirmReservationForPortfolioCommand command =
                new ConfirmReservationForPortfolioCommand(portfolioIdentifier, itemIdentifier, 100);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()),
                new ItemsAddedToPortfolioEvent(itemIdentifier, 400),
                new ItemsReservedEvent(itemIdentifier, 100))
                .when(command)
                .expectEvents(new ReservationConfirmedForPortfolioEvent(itemIdentifier, 100));
    }

    @Test
    public void testCancellationOfReservation() {
        AggregateIdentifier portfolioIdentifier = fixture.getAggregateIdentifier();
        AggregateIdentifier itemIdentifier = new UUIDAggregateIdentifier();

        CancelReservationForPortfolioCommand command =
                new CancelReservationForPortfolioCommand(portfolioIdentifier, itemIdentifier, 100);
        fixture.given(new PortfolioCreatedEvent(new UUIDAggregateIdentifier()),
                new ItemsAddedToPortfolioEvent(itemIdentifier, 400),
                new ItemsReservedEvent(itemIdentifier, 100))
                .when(command)
                .expectEvents(new ReservationCancelledForPortfolioEvent(itemIdentifier, 100));
    }

    /* Money related test methods */
    @Ignore
    public void testAddingMoneyToThePortfolio() {

    }

    @Ignore
    public void testMakingPaymentFromPortfolio() {

    }

    @Ignore
    public void testMakingPaymentFromPortfolio_withoutEnoughMoney() {

    }

}