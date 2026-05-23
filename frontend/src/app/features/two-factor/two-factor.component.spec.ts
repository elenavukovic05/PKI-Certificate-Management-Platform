import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TwoFactorModalComponent } from './two-factor.component';

describe('TwoFactorComponent', () => {
  let component: TwoFactorModalComponent;
  let fixture: ComponentFixture<TwoFactorModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TwoFactorModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TwoFactorModalComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
