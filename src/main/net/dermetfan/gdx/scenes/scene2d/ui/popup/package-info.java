/** This package contains a flexible popup API for things like tooltips, context menus and menu bars.
 *  <p>
 *      The {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup Popup} class is an EventListener with a popup Actor that can be {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup#show() shown} and {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup#hide() hidden}.<br>
 *      A Popup is controlled via its {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup#behavior Behavior}. The {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior} is an interface for actual show and hide implementations which are triggered by the {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior.Reaction} returned by {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior#handle(com.badlogic.gdx.scenes.scene2d.Event, net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup) handle(Event, Popup)}.
 *  </p>
 *  <p>
 *      {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.BehaviorMultiplexer BehaviorMultiplexers} can be used to combine multiple Behaviors. There are already a few Behaviors such as the {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.VisibilityBehavior VisibilityBehavior} (which shows/hides using visibility), the {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.FadeBehavior FadeBehavior} (which shows/hides using {@link com.badlogic.gdx.scenes.scene2d.actions.AlphaAction AlphaActions}) and the {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.PositionBehavior PositionBehavior}.<br>
 *      The PositionBehavior positions the {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup#popup} in {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior#show(com.badlogic.gdx.scenes.scene2d.Event, net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup) show(Event, Popup)}. The actual position is applied by its {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.PositionBehavior#position}. Positions can be combined using a {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.PositionMultiplexer} which {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.PositionBehavior.Position#apply(com.badlogic.gdx.scenes.scene2d.Event, com.badlogic.gdx.scenes.scene2d.Actor) applies} each Position in a sequence.
 *  </p>
 *  <p>
 *      Examples:
 *      <ul>
 *          <li>
 *              Menu bar menu:<br>
 *              {@code new Popup<>(menu, new BehaviorMultiplexer(new MenuBehavior(), new PositionBehavior(new AlignPosition(Align.bottomLeft, Align.topLeft)), new VisibilityBehavior()))}
 *          </li>
 *          <li>
 *              Context menu:<br>
 *              {@code new Popup<>(contextMenu, new BehaviorMultiplexer(new MenuBehavior(), new PositionBehavior(new AlignPosition(Align.topRight, Align.topLeft)), new VisibilityBehavior()))}<br>
 *              <em>The only difference to the above example is the first parameter of the AlignPosition constructor.</em>
 *          </li>
 *          <li>
 *              Tooltip:<br>
 *              {@code new Popup<>(tooltip, new BehaviorMultiplexer(new TooltipBehavior(), new PositionBehavior(new PositionMultiplexer(new PointerPosition(), new AlignedOffsetPosition(Align.topLeft))), new VisibilityBehavior()))}
 *          </li>
 *      </ul>
 *  </p>
 *  <p>
 *      Some Behaviors (like the {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.MenuBehavior} and {@link net.dermetfan.gdx.scenes.scene2d.ui.popup.TooltipBehavior}) only function properly if their Popup is not only added to the targeted Actor but also to something high in the event hierarchy (like the Stage).
 *      This is so that they receive events from other (usually all) actors and can react accordingly (for example hide when other Actors are clicked). In that case you may add them directly to an Actor or the Stage or use an {@link net.dermetfan.gdx.scenes.scene2d.EventMultiplexer EventMultiplexer} (which of course should reside high in the event hierarchy).
 *      Those Behaviors usually state so in their documentation. If you add a Popup using a Behavior not designed for this to two Actors it may not function properly.
 *  </p>
 *  @author dermetfan
 *  @since 0.8.0 */
package net.dermetfan.gdx.scenes.scene2d.ui.popup;